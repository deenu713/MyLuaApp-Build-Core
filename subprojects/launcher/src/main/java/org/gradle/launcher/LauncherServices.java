package org.gradle.launcher;


import org.gradle.execution.WorkValidationWarningReporter;
import org.gradle.initialization.BuildRequestMetaData;
import org.gradle.initialization.exception.ExceptionAnalyser;
import org.gradle.internal.build.BuildStateRegistry;
import org.gradle.internal.buildevents.BuildStartedTime;
import org.gradle.internal.buildtree.BuildActionRunner;
import org.gradle.internal.buildtree.BuildTreeActionExecutor;
import org.gradle.internal.buildtree.ProblemReportingBuildActionRunner;
import org.gradle.internal.event.ListenerManager;
import org.gradle.internal.filewatch.DefaultFileSystemChangeWaiterFactory;
import org.gradle.internal.filewatch.FileSystemChangeWaiterFactory;
import org.gradle.internal.filewatch.FileWatcherFactory;
import org.gradle.internal.logging.text.StyledTextOutputFactory;
import org.gradle.internal.operations.BuildOperationProgressEventEmitter;
import org.gradle.internal.service.ServiceRegistration;
import org.gradle.internal.service.scopes.AbstractPluginServiceRegistry;
import org.gradle.internal.time.Clock;
import org.gradle.launcher.exec.BuildCompletionNotifyingBuildActionRunner;
import org.gradle.launcher.exec.BuildOutcomeReportingBuildActionRunner;
import org.gradle.launcher.exec.ChainingBuildActionRunner;
import org.gradle.launcher.exec.RootBuildLifecycleBuildActionExecutor;
import org.gradle.problems.buildtree.ProblemReporter;

import java.util.List;

public class LauncherServices extends AbstractPluginServiceRegistry {
    @Override
    public void registerGlobalServices(ServiceRegistration registration) {
        registration.addProvider(new ToolingGlobalScopeServices());
    }

    @Override
    public void registerGradleUserHomeServices(ServiceRegistration registration) {

    }

    @Override
    public void registerBuildSessionServices(ServiceRegistration registration) {

    }

    @Override
    public void registerBuildTreeServices(ServiceRegistration registration) {
        registration.addProvider(new ToolingBuildTreeScopeServices());
    }

    static class ToolingGlobalScopeServices {


        FileSystemChangeWaiterFactory createFileSystemChangeWaiterFactory(FileWatcherFactory fileWatcherFactory) {
            return new DefaultFileSystemChangeWaiterFactory(fileWatcherFactory);
        }

    }


    static class ToolingBuildTreeScopeServices {
        BuildTreeActionExecutor createActionExecutor(List<BuildActionRunner> buildActionRunners,
                                                     StyledTextOutputFactory styledTextOutputFactory,
                                                     BuildStateRegistry buildStateRegistry,
                                                     BuildOperationProgressEventEmitter eventEmitter,
                                                     WorkValidationWarningReporter workValidationWarningReporter,
                                                     ListenerManager listenerManager,
                                                     BuildStartedTime buildStartedTime,
                                                     BuildRequestMetaData buildRequestMetaData,
                                                     Clock clock,
                                                     ExceptionAnalyser exceptionAnalyser,
                                                     List<ProblemReporter> problemReporters) {
            return new RootBuildLifecycleBuildActionExecutor(
                    buildStateRegistry,
                    new BuildCompletionNotifyingBuildActionRunner(
                            new BuildOutcomeReportingBuildActionRunner(
                                    styledTextOutputFactory,
                                    workValidationWarningReporter,
                                    listenerManager,
                                    new ProblemReportingBuildActionRunner(
                                            new ChainingBuildActionRunner(buildActionRunners),
                                            exceptionAnalyser,
                                            problemReporters
                                    ),
                                    buildStartedTime,
                                    buildRequestMetaData,
                                    clock)));
        }
    }
}
