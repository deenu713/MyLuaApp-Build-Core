package org.gradle.tooling.internal.provider;

import org.gradle.api.execution.internal.TaskInputsListeners;
import org.gradle.deployment.internal.DeploymentRegistryInternal;
import org.gradle.execution.WorkValidationWarningReporter;
import org.gradle.initialization.BuildCancellationToken;
import org.gradle.initialization.BuildEventConsumer;
import org.gradle.initialization.BuildRequestMetaData;
import org.gradle.initialization.exception.ExceptionAnalyser;
import org.gradle.internal.build.BuildStateRegistry;
import org.gradle.internal.buildevents.BuildStartedTime;
import org.gradle.internal.buildtree.BuildActionRunner;
import org.gradle.internal.buildtree.BuildTreeActionExecutor;
import org.gradle.internal.buildtree.BuildTreeModelControllerServices;
import org.gradle.internal.buildtree.ProblemReportingBuildActionRunner;
import org.gradle.internal.classpath.CachedClasspathTransformer;
import org.gradle.internal.concurrent.ExecutorFactory;
import org.gradle.internal.event.ListenerManager;
import org.gradle.internal.filewatch.DefaultFileSystemChangeWaiterFactory;
import org.gradle.internal.filewatch.FileSystemChangeWaiterFactory;
import org.gradle.internal.filewatch.FileWatcherFactory;
import org.gradle.internal.logging.LoggingManagerInternal;
import org.gradle.internal.logging.text.StyledTextOutputFactory;
import org.gradle.internal.operations.BuildOperationExecutor;
import org.gradle.internal.operations.BuildOperationListenerManager;
import org.gradle.internal.operations.BuildOperationProgressEventEmitter;
import org.gradle.internal.operations.logging.LoggingBuildOperationProgressBroadcaster;
import org.gradle.internal.operations.notify.BuildOperationNotificationValve;
import org.gradle.internal.service.ServiceRegistration;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.internal.service.scopes.AbstractPluginServiceRegistry;
import org.gradle.internal.service.scopes.GradleUserHomeScopeServiceRegistry;
import org.gradle.internal.session.BuildSessionActionExecutor;
import org.gradle.internal.time.Clock;
import org.gradle.internal.time.Time;
import org.gradle.internal.work.WorkerLeaseService;
import org.gradle.launcher.exec.BuildCompletionNotifyingBuildActionRunner;
import org.gradle.launcher.exec.BuildExecuter;
import org.gradle.launcher.exec.BuildOutcomeReportingBuildActionRunner;
import org.gradle.launcher.exec.BuildTreeLifecycleBuildActionExecutor;
import org.gradle.launcher.exec.ChainingBuildActionRunner;
import org.gradle.launcher.exec.RootBuildLifecycleBuildActionExecutor;
import org.gradle.launcher.exec.RunAsBuildOperationBuildActionExecutor;
import org.gradle.launcher.exec.RunAsWorkerThreadBuildActionExecutor;
import org.gradle.problems.buildtree.ProblemReporter;


import java.util.List;

public class LauncherServices extends AbstractPluginServiceRegistry {

    @Override
    public void registerGlobalServices(ServiceRegistration registration) {
        registration.addProvider(new ToolingGlobalScopeServices());
    }

    @Override
    public void registerGradleUserHomeServices(ServiceRegistration registration) {
        registration.addProvider(new ToolingGradleUserHomeScopeServices());
    }

    @Override
    public void registerBuildTreeServices(ServiceRegistration registration) {
        registration.addProvider(new ToolingBuildTreeScopeServices());
    }

    @Override
    public void registerBuildSessionServices(ServiceRegistration registration) {
        registration.addProvider(new ToolingBuildSessionScopeServices());
    }


    static class ToolingGlobalScopeServices {

        BuildExecuter createBuildExecuter(StyledTextOutputFactory styledTextOutputFactory,
                                          LoggingManagerInternal loggingManager,
                                          WorkValidationWarningReporter workValidationWarningReporter,
                                          GradleUserHomeScopeServiceRegistry userHomeServiceRegistry,
                                          ServiceRegistry globalServices) {
            // @formatter:off
            return new SetupLoggingActionExecuter(loggingManager,
                    new SessionFailureReportingActionExecuter(styledTextOutputFactory, Time.clock(),
                            workValidationWarningReporter, new StartParamsValidatingActionExecuter(
                            new BuildSessionLifecycleBuildActionExecuter(userHomeServiceRegistry,
                                    globalServices))));
            // @formatter:on
        }


        FileSystemChangeWaiterFactory createFileSystemChangeWaiterFactory(FileWatcherFactory fileWatcherFactory) {
            return new DefaultFileSystemChangeWaiterFactory(fileWatcherFactory);
        }

        ExecuteBuildActionRunner createExecuteBuildActionRunner() {
            return new ExecuteBuildActionRunner();
        }


    }

    static class ToolingGradleUserHomeScopeServices {
    }

    static class ToolingBuildSessionScopeServices {

        BuildSessionActionExecutor createActionExecutor(
//                BuildEventListenerFactory listenerFactory,
                ExecutorFactory executorFactory,
                ListenerManager listenerManager,
                BuildOperationListenerManager buildOperationListenerManager,
                BuildOperationExecutor buildOperationExecutor,
                TaskInputsListeners inputsListeners,
                StyledTextOutputFactory styledTextOutputFactory,
                FileSystemChangeWaiterFactory fileSystemChangeWaiterFactory,
                BuildRequestMetaData requestMetaData,
                BuildCancellationToken cancellationToken,
                DeploymentRegistryInternal deploymentRegistry,
                BuildEventConsumer eventConsumer,
                BuildStartedTime buildStartedTime,
                Clock clock,
                LoggingBuildOperationProgressBroadcaster loggingBuildOperationProgressBroadcaster,
                BuildOperationNotificationValve buildOperationNotificationValve,
                BuildTreeModelControllerServices buildModelServices,
                WorkerLeaseService workerLeaseService
                /* BuildLayoutValidator buildLayoutValidator*/) {
            return new SubscribableBuildActionExecutor(listenerManager, buildOperationListenerManager, eventConsumer,
                    new ContinuousBuildActionExecutor(fileSystemChangeWaiterFactory, inputsListeners, styledTextOutputFactory, executorFactory, requestMetaData, cancellationToken, deploymentRegistry, listenerManager, buildStartedTime, clock,
                            new RunAsWorkerThreadBuildActionExecutor(
                                    workerLeaseService,
                                    new RunAsBuildOperationBuildActionExecutor(
                                            new BuildTreeLifecycleBuildActionExecutor(buildModelServices), buildOperationExecutor, loggingBuildOperationProgressBroadcaster, buildOperationNotificationValve))));
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
                            new FileSystemWatchingBuildActionRunner(eventEmitter,
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
                                            clock))));
        }
    }

}
