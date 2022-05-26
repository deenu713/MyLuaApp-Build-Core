/*
 * Copyright 2021 the original author or authors.
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
package org.gradle.internal.buildtree;

import org.gradle.api.internal.GradleInternal;
import org.gradle.api.internal.SettingsInternal;
import org.gradle.composite.internal.IncludedBuildTaskGraph;
import org.gradle.initialization.exception.ExceptionAnalyser;
import org.gradle.internal.build.BuildLifecycleController;
import org.gradle.internal.build.ExecutionResult;

import java.util.function.Function;
import java.util.function.Supplier;

public class DefaultBuildTreeLifecycleController implements BuildTreeLifecycleController {
    private boolean completed;
    private final BuildLifecycleController buildLifecycleController;
    private final IncludedBuildTaskGraph taskGraph;
    private final BuildTreeWorkPreparer workPreparer;
    private final BuildTreeWorkExecutor workExecutor;
    private final BuildTreeModelCreator modelCreator;
    private final BuildTreeFinishExecutor finishExecutor;
    private final ExceptionAnalyser exceptionAnalyser;

    public DefaultBuildTreeLifecycleController(BuildLifecycleController buildLifecycleController,
                                               IncludedBuildTaskGraph taskGraph,
                                               BuildTreeWorkPreparer workPreparer,
                                               BuildTreeWorkExecutor workExecutor,
                                               BuildTreeModelCreator modelCreator,
                                               BuildTreeFinishExecutor finishExecutor,
                                               ExceptionAnalyser exceptionAnalyser) {
        this.buildLifecycleController = buildLifecycleController;
        this.taskGraph = taskGraph;
        this.workPreparer = workPreparer;
        this.modelCreator = modelCreator;
        this.workExecutor = workExecutor;
        this.finishExecutor = finishExecutor;
        this.exceptionAnalyser = exceptionAnalyser;
    }

    @Override
    public GradleInternal getGradle() {
        if (completed) {
            throw new IllegalStateException("Cannot use Gradle object after build has finished.");
        }
        return buildLifecycleController.getGradle();
    }

    @Override
    public void scheduleAndRunTasks() {
        runBuild(this::doScheduleAndRunTasks);
    }

    @Override
    public <T> T fromBuildModel(boolean runTasks, Function<? super GradleInternal, T> action) {
        return runBuild(() -> {
            if (runTasks) {
                ExecutionResult<Void> result = doScheduleAndRunTasks();
                if (!result.getFailures().isEmpty()) {
                    return result.asFailure();
                }
            }
            T model = modelCreator.fromBuildModel(action);
            return ExecutionResult.succeeded(model);
        });
    }

    private ExecutionResult<Void> doScheduleAndRunTasks() {
        return taskGraph.withNewTaskGraph(() -> {
            workPreparer.scheduleRequestedTasks();
            return workExecutor.execute();
        });
    }

    @Override
    public <T> T withEmptyBuild(Function<? super SettingsInternal, T> action) {
        return runBuild(() -> {
            T result = action.apply(buildLifecycleController.getLoadedSettings());
            return ExecutionResult.succeeded(result);
        });
    }

    private <T> T runBuild(Supplier<ExecutionResult<? extends T>> action) {
        if (completed) {
            throw new IllegalStateException("Cannot run more than one action for this build.");
        }
        completed = true;

        ExecutionResult<? extends T> result;
        try {
            result = action.get();
        } catch (Throwable t) {
            result = ExecutionResult.failed(t);
        }

        ExecutionResult<Void> finishResult = finishExecutor.finishBuildTree(result.getFailures());
        result = result.withFailures(finishResult);

        RuntimeException finalReportableFailure = exceptionAnalyser.transform(result.getFailures());
        if (finalReportableFailure != null) {
            throw finalReportableFailure;
        }

        return result.getValue();
    }
}
