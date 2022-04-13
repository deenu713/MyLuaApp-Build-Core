/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.internal.service.scopes;

import org.gradle.api.internal.DocumentationRegistry;
import org.gradle.api.internal.cache.StringInterner;
import org.gradle.api.internal.changedetection.state.DefaultExecutionHistoryCacheAccess;
import org.gradle.api.invocation.Gradle;
import org.gradle.cache.CacheBuilder;
import org.gradle.cache.CacheRepository;
import org.gradle.cache.FileLockManager;
import org.gradle.cache.PersistentCache;
import org.gradle.cache.internal.InMemoryCacheDecoratorFactory;
import org.gradle.caching.internal.controller.BuildCacheCommandFactory;
import org.gradle.caching.internal.controller.BuildCacheController;
import org.gradle.concurrent.ParallelismConfiguration;
import org.gradle.execution.plan.DefaultPlanExecutor;
import org.gradle.execution.plan.PlanExecutor;
import org.gradle.initialization.BuildCancellationToken;
import org.gradle.internal.concurrent.ExecutorFactory;
import org.gradle.internal.enterprise.core.GradleEnterprisePluginManager;
import org.gradle.internal.event.ListenerManager;
import org.gradle.internal.execution.ExecutionEngine;
import org.gradle.internal.execution.OutputChangeListener;
import org.gradle.internal.execution.OutputSnapshotter;
import org.gradle.internal.execution.history.ExecutionHistoryCacheAccess;
import org.gradle.internal.execution.history.ExecutionHistoryStore;
import org.gradle.internal.execution.history.OutputFilesRepository;
import org.gradle.internal.execution.history.OverlappingOutputDetector;
import org.gradle.internal.execution.history.changes.ExecutionStateChangeDetector;
import org.gradle.internal.execution.history.impl.DefaultExecutionHistoryStore;
import org.gradle.internal.execution.history.impl.DefaultOutputFilesRepository;
import org.gradle.internal.execution.impl.DefaultExecutionEngine;
import org.gradle.internal.execution.steps.AssignWorkspaceStep;
import org.gradle.internal.execution.steps.BroadcastChangingOutputsStep;
import org.gradle.internal.execution.steps.BuildCacheStep;
import org.gradle.internal.execution.steps.CancelExecutionStep;
import org.gradle.internal.execution.steps.CaptureStateAfterExecutionStep;
import org.gradle.internal.execution.steps.CaptureStateBeforeExecutionStep;
import org.gradle.internal.execution.steps.CreateOutputsStep;
import org.gradle.internal.execution.steps.ExecuteStep;
import org.gradle.internal.execution.steps.IdentifyStep;
import org.gradle.internal.execution.steps.IdentityCacheStep;
import org.gradle.internal.execution.steps.LoadExecutionStateStep;
import org.gradle.internal.execution.steps.RecordOutputsStep;
import org.gradle.internal.execution.steps.RemovePreviousOutputsStep;
import org.gradle.internal.execution.steps.ResolveCachingStateStep;
import org.gradle.internal.execution.steps.ResolveChangesStep;
import org.gradle.internal.execution.steps.ResolveInputChangesStep;
import org.gradle.internal.execution.steps.SkipEmptyWorkStep;
import org.gradle.internal.execution.steps.SkipUpToDateStep;
import org.gradle.internal.execution.steps.StoreExecutionStateStep;
import org.gradle.internal.execution.steps.TimeoutStep;
import org.gradle.internal.execution.steps.ValidateStep;
import org.gradle.internal.execution.steps.legacy.MarkSnapshottingInputsFinishedStep;
import org.gradle.internal.execution.steps.legacy.MarkSnapshottingInputsStartedStep;
import org.gradle.internal.execution.timeout.TimeoutHandler;
import org.gradle.internal.file.Deleter;
import org.gradle.internal.hash.ClassLoaderHierarchyHasher;
import org.gradle.internal.operations.BuildOperationExecutor;
import org.gradle.internal.operations.CurrentBuildOperationRef;
import org.gradle.internal.resources.ResourceLockCoordinationService;
import org.gradle.internal.resources.SharedResourceLeaseRegistry;
import org.gradle.internal.scopeids.id.BuildInvocationScopeId;
import org.gradle.internal.vfs.VirtualFileSystem;
import org.gradle.internal.work.WorkerLeaseService;
import org.gradle.util.GradleVersion;

import java.util.Collections;

import static org.gradle.cache.internal.filelock.LockOptionsBuilder.mode;

public class ExecutionGradleServices {
    ExecutionHistoryCacheAccess createCacheAccess(Gradle gradle, CacheRepository cacheRepository) {
        return new DefaultExecutionHistoryCacheAccess(gradle, cacheRepository);
    }

    ExecutionHistoryStore createExecutionHistoryStore(
        ExecutionHistoryCacheAccess executionHistoryCacheAccess,
        InMemoryCacheDecoratorFactory inMemoryCacheDecoratorFactory,
        StringInterner stringInterner
    ) {
        return new DefaultExecutionHistoryStore(
            executionHistoryCacheAccess,
            inMemoryCacheDecoratorFactory,
            stringInterner
        );
    }

    OutputFilesRepository createOutputFilesRepository(CacheRepository cacheRepository, Gradle gradle, InMemoryCacheDecoratorFactory inMemoryCacheDecoratorFactory) {
        PersistentCache cacheAccess = cacheRepository
            .cache(gradle, "buildOutputCleanup")
            .withCrossVersionCache(CacheBuilder.LockTarget.DefaultTarget)
            .withDisplayName("Build Output Cleanup Cache")
            .withLockOptions(mode(FileLockManager.LockMode.OnDemand))
            .withProperties(Collections.singletonMap("gradle.version", GradleVersion.current().getVersion()))
            .open();
        return new DefaultOutputFilesRepository(cacheAccess, inMemoryCacheDecoratorFactory);
    }

    PlanExecutor createPlanExecutor(
        ParallelismConfiguration parallelismConfiguration,
        ExecutorFactory executorFactory,
        WorkerLeaseService workerLeaseService,
        BuildCancellationToken cancellationToken,
        ResourceLockCoordinationService coordinationService) {
        int parallelThreads = parallelismConfiguration.getMaxWorkerCount();
        if (parallelThreads < 1) {
            throw new IllegalStateException(String.format("Cannot create executor for requested number of worker threads: %s.", parallelThreads));
        }

        return new DefaultPlanExecutor(
            parallelismConfiguration,
            executorFactory,
            workerLeaseService,
            cancellationToken,
            coordinationService
        );
    }

    OutputChangeListener createOutputChangeListener(ListenerManager listenerManager) {
        return listenerManager.getBroadcaster(OutputChangeListener.class);
    }

    public ExecutionEngine createExecutionEngine(
        BuildCacheCommandFactory buildCacheCommandFactory,
        BuildCacheController buildCacheController,
        BuildCancellationToken cancellationToken,
        BuildInvocationScopeId buildInvocationScopeId,
        BuildOperationExecutor buildOperationExecutor,
        GradleEnterprisePluginManager gradleEnterprisePluginManager,
        ClassLoaderHierarchyHasher classLoaderHierarchyHasher,
        CurrentBuildOperationRef currentBuildOperationRef,
        Deleter deleter,
        ExecutionStateChangeDetector changeDetector,
        OutputChangeListener outputChangeListener,
        OutputFilesRepository outputFilesRepository,
        OutputSnapshotter outputSnapshotter,
        OverlappingOutputDetector overlappingOutputDetector,
        TimeoutHandler timeoutHandler,
        ValidateStep.ValidationWarningRecorder validationWarningRecorder,
        VirtualFileSystem virtualFileSystem,
        DocumentationRegistry documentationRegistry
    ) {
        // @formatter:off
        return new DefaultExecutionEngine(documentationRegistry,
            new IdentifyStep<>(
            new IdentityCacheStep<>(
            new AssignWorkspaceStep<>(
            new LoadExecutionStateStep<>(
            new MarkSnapshottingInputsStartedStep<>(
            new SkipEmptyWorkStep<>(
            new CaptureStateBeforeExecutionStep(buildOperationExecutor, classLoaderHierarchyHasher, outputSnapshotter, overlappingOutputDetector,
            new ValidateStep<>(virtualFileSystem, validationWarningRecorder,
            new ResolveCachingStateStep(buildCacheController, gradleEnterprisePluginManager.isPresent(),
            new MarkSnapshottingInputsFinishedStep<>(
            new ResolveChangesStep<>(changeDetector,
            new SkipUpToDateStep<>(
            new RecordOutputsStep<>(outputFilesRepository,
            new StoreExecutionStateStep<>(
            new BuildCacheStep(buildCacheController, buildCacheCommandFactory, deleter, outputChangeListener,
            new BroadcastChangingOutputsStep<>(outputChangeListener,
            new CaptureStateAfterExecutionStep<>(buildOperationExecutor, buildInvocationScopeId.getId(), outputSnapshotter,
            new CreateOutputsStep<>(
            new TimeoutStep<>(timeoutHandler, currentBuildOperationRef,
            new CancelExecutionStep<>(cancellationToken,
            new ResolveInputChangesStep<>(
            new RemovePreviousOutputsStep<>(deleter, outputChangeListener,
            new ExecuteStep<>(buildOperationExecutor
        ))))))))))))))))))))))));
        // @formatter:on
    }

    SharedResourceLeaseRegistry createSharedResourceLeaseRegistry(ResourceLockCoordinationService coordinationService) {
        return new SharedResourceLeaseRegistry(coordinationService);
    }
}
