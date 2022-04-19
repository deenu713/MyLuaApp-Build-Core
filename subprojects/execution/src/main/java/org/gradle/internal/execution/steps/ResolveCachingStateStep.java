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

package org.gradle.internal.execution.steps;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import org.gradle.caching.BuildCacheKey;
import org.gradle.caching.internal.controller.BuildCacheController;
import org.gradle.caching.internal.origin.OriginMetadata;
import org.gradle.internal.Try;
import org.gradle.internal.execution.ExecutionResult;
import org.gradle.internal.execution.UnitOfWork;
import org.gradle.internal.execution.WorkValidationContext;
import org.gradle.internal.execution.caching.CachingDisabledReason;
import org.gradle.internal.execution.caching.CachingDisabledReasonCategory;
import org.gradle.internal.execution.caching.CachingState;
import org.gradle.internal.execution.caching.CachingStateBuilder;
import org.gradle.internal.execution.caching.impl.DefaultCachingStateBuilder;
import org.gradle.internal.execution.caching.impl.LoggingCachingStateBuilder;
import org.gradle.internal.execution.history.AfterPreviousExecutionState;
import org.gradle.internal.execution.history.BeforeExecutionState;
import org.gradle.internal.execution.history.ExecutionHistoryStore;
import org.gradle.internal.execution.history.OverlappingOutputs;
import org.gradle.internal.fingerprint.CurrentFileCollectionFingerprint;
import org.gradle.internal.snapshot.FileSystemSnapshot;
import org.gradle.internal.snapshot.ValueSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Duration;
import java.util.Formatter;
import java.util.List;
import java.util.Optional;

public class ResolveCachingStateStep implements Step<ValidationFinishedContext, CachingResult> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResolveCachingStateStep.class);
    private static final CachingDisabledReason BUILD_CACHE_DISABLED_REASON = new CachingDisabledReason(CachingDisabledReasonCategory.BUILD_CACHE_DISABLED, "Build cache is disabled");
    private static final CachingState BUILD_CACHE_DISABLED_STATE = CachingState.disabledWithoutInputs(BUILD_CACHE_DISABLED_REASON);
    private static final CachingDisabledReason VALIDATION_FAILED_REASON = new CachingDisabledReason(CachingDisabledReasonCategory.VALIDATION_FAILURE, "Caching has been disabled to ensure correctness. Please consult deprecation warnings for more details.");
    private static final CachingState VALIDATION_FAILED_STATE = CachingState.disabledWithoutInputs(VALIDATION_FAILED_REASON);

    private final BuildCacheController buildCache;
    private final boolean buildScansEnabled;
    private final Step<? super CachingContext, ? extends UpToDateResult> delegate;

    public ResolveCachingStateStep(
        BuildCacheController buildCache,
        boolean buildScansEnabled,
        Step<? super CachingContext, ? extends UpToDateResult> delegate
    ) {
        this.buildCache = buildCache;
        this.buildScansEnabled = buildScansEnabled;
        this.delegate = delegate;
    }

    @Override
    public CachingResult execute(UnitOfWork work, ValidationFinishedContext context) {
        CachingState cachingState;
        if (!buildCache.isEnabled() && !buildScansEnabled) {
            cachingState = BUILD_CACHE_DISABLED_STATE;
        } else if (context.getValidationProblems().isPresent()) {
            cachingState = VALIDATION_FAILED_STATE;
        } else {
            cachingState = context.getBeforeExecutionState()
                .map(beforeExecutionState -> calculateCachingState(beforeExecutionState, work))
                .orElseGet(() -> calculateCachingStateWithNoCapturedInputs(work));
        }

        ImmutableList<CachingDisabledReason> disabledReasons = cachingState.getDisabledReasons();
        if (disabledReasons.isEmpty()) {
            //noinspection OptionalGetWithoutIsPresent
            logCacheKey(cachingState.getKey().get(), work);
        } else {
            logDisabledReasons(disabledReasons, work);
        }

        UpToDateResult result = delegate.execute(work, new CachingContext() {
            @Override
            public CachingState getCachingState() {
                return cachingState;
            }

            @Override
            public Optional<String> getRebuildReason() {
                return context.getRebuildReason();
            }

            @Override
            public WorkValidationContext getValidationContext() {
                return context.getValidationContext();
            }

            @Override
            public ImmutableSortedMap<String, ValueSnapshot> getInputProperties() {
                return context.getInputProperties();
            }

            @Override
            public ImmutableSortedMap<String, CurrentFileCollectionFingerprint> getInputFileProperties() {
                return context.getInputFileProperties();
            }

            @Override
            public UnitOfWork.Identity getIdentity() {
                return context.getIdentity();
            }

            @Override
            public File getWorkspace() {
                return context.getWorkspace();
            }

            @Override
            public Optional<ExecutionHistoryStore> getHistory() {
                return context.getHistory();
            }

            @Override
            public Optional<AfterPreviousExecutionState> getAfterPreviousExecutionState() {
                return context.getAfterPreviousExecutionState();
            }

            @Override
            public Optional<ValidationResult> getValidationProblems() {
                return context.getValidationProblems();
            }

            @Override
            public Optional<BeforeExecutionState> getBeforeExecutionState() {
                return context.getBeforeExecutionState();
            }
        });
        return new CachingResult() {
            @Override
            public CachingState getCachingState() {
                return cachingState;
            }

            @Override
            public ImmutableList<String> getExecutionReasons() {
                return result.getExecutionReasons();
            }

            @Override
            public ImmutableSortedMap<String, FileSystemSnapshot> getOutputFilesProduceByWork() {
                return result.getOutputFilesProduceByWork();
            }

            @Override
            public Optional<OriginMetadata> getReusedOutputOriginMetadata() {
                return result.getReusedOutputOriginMetadata();
            }

            @Override
            public Try<ExecutionResult> getExecutionResult() {
                return result.getExecutionResult();
            }

            @Override
            public Duration getDuration() {
                return result.getDuration();
            }
        };
    }

    private CachingState calculateCachingState(BeforeExecutionState executionState, UnitOfWork work) {
        CachingStateBuilder builder = buildCache.isEmitDebugLogging()
            ? new LoggingCachingStateBuilder()
            : new DefaultCachingStateBuilder();

        if (!buildCache.isEnabled()) {
            builder.markNotCacheable(BUILD_CACHE_DISABLED_REASON);
        }
        OverlappingOutputs detectedOverlappingOutputs = executionState.getDetectedOverlappingOutputs()
            .orElse(null);
        work.shouldDisableCaching(detectedOverlappingOutputs)
            .ifPresent(builder::markNotCacheable);

        builder.withImplementation(executionState.getImplementation());
        builder.withAdditionalImplementations(executionState.getAdditionalImplementations());
        builder.withInputValueFingerprints(executionState.getInputProperties());
        builder.withInputFilePropertyFingerprints(executionState.getInputFileProperties());
        builder.withOutputPropertyNames(executionState.getOutputFileLocationSnapshots().keySet());

        return builder.build();
    }

    private CachingState calculateCachingStateWithNoCapturedInputs(UnitOfWork work) {
        if (!buildCache.isEnabled()) {
            return BUILD_CACHE_DISABLED_STATE;
        }
        return work.shouldDisableCaching(null)
            .map(CachingState::disabledWithoutInputs)
            .orElse(CachingState.NOT_DETERMINED);
    }

    private void logCacheKey(BuildCacheKey cacheKey, UnitOfWork work) {
        if (buildCache.isEmitDebugLogging()) {
            LOGGER.warn("Build cache key for {} is {}", work.getDisplayName(), cacheKey.getDisplayName());
        } else {
            LOGGER.info("Build cache key for {} is {}", work.getDisplayName(), cacheKey.getDisplayName());
        }
    }

    private void logDisabledReasons(List<CachingDisabledReason> reasons, UnitOfWork work) {
        if (LOGGER.isInfoEnabled()) {
            Formatter formatter = new Formatter();
            formatter.format("Caching disabled for %s because:", work.getDisplayName());
            for (CachingDisabledReason reason : reasons) {
                formatter.format("%n  %s", reason.getMessage());
            }
            LOGGER.info(formatter.toString());
        }
    }
}
