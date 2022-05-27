package org.gradle.launcher;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;

import org.gradle.BuildAdapter;
import org.gradle.BuildListener;
import org.gradle.BuildResult;
import org.gradle.StartParameter;
import org.gradle.api.Action;
import org.gradle.api.initialization.Settings;
import org.gradle.api.internal.StartParameterInternal;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.configuration.LoggingConfiguration;
import org.gradle.configuration.GradleLauncherMetaData;
import org.gradle.initialization.BuildCancellationToken;
import org.gradle.initialization.BuildClientMetaData;
import org.gradle.initialization.BuildEventConsumer;
import org.gradle.initialization.BuildLayoutParameters;
import org.gradle.initialization.BuildRequestContext;
import org.gradle.initialization.BuildRequestMetaData;
import org.gradle.initialization.DefaultBuildCancellationToken;
import org.gradle.initialization.DefaultBuildRequestContext;
import org.gradle.initialization.DefaultBuildRequestMetaData;
import org.gradle.initialization.ReportedException;
import org.gradle.internal.SystemProperties;
import org.gradle.internal.UncheckedException;
import org.gradle.internal.buildtree.BuildActionRunner;
import org.gradle.internal.classpath.ClassPath;
import org.gradle.internal.concurrent.CompositeStoppable;
import org.gradle.internal.concurrent.Stoppable;
import org.gradle.internal.event.ListenerManager;
import org.gradle.internal.invocation.BuildAction;
import org.gradle.internal.logging.DefaultLoggingConfiguration;
import org.gradle.internal.logging.LoggingManagerInternal;
import org.gradle.internal.logging.services.LoggingServiceRegistry;
import org.gradle.internal.nativeintegration.services.NativeServices;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.internal.service.ServiceRegistryBuilder;
import org.gradle.internal.service.scopes.GlobalScopeServices;
import org.gradle.internal.service.scopes.GradleUserHomeScopeServiceRegistry;
import org.gradle.internal.service.scopes.WorkerSharedGlobalScopeServices;
import org.gradle.internal.session.BuildSessionState;
import org.gradle.internal.session.CrossBuildSessionState;
import org.gradle.internal.vfs.FileSystemAccess;
import org.gradle.internal.vfs.VirtualFileSystem;
import org.gradle.launcher.bootstrap.ExecutionListener;
import org.gradle.launcher.cli.DebugLoggerWarningAction;
import org.gradle.launcher.cli.ExceptionReportingAction;
import org.gradle.launcher.cli.ExecuteBuildAction;
import org.gradle.launcher.cli.NativeServicesInitializingAction;
import org.gradle.launcher.cli.RunBuildAction;
import org.gradle.launcher.cli.WelcomeMessageAction;
import org.gradle.launcher.configuration.BuildLayoutResult;
import org.gradle.launcher.exec.BuildActionExecuter;
import org.gradle.launcher.exec.BuildActionParameters;
import org.gradle.launcher.exec.BuildActionResult;
import org.gradle.launcher.exec.BuildExecuter;
import org.gradle.launcher.exec.DefaultBuildActionParameters;
import org.gradle.testfixtures.internal.ProjectBuilderImpl;
import org.gradle.testfixtures.internal.TestGlobalScopeServices;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public class TestGradleLauncher {


    private StartParameterInternal startParameter;

    private static ServiceRegistry globalServices;

    TestGradleLauncher(StartParameterInternal startParameter) {
        this.startParameter = startParameter;

        if (globalServices == null) {
            globalServices = ServiceRegistryBuilder
                    .builder()
                    .displayName("global services")
                    .parent(LoggingServiceRegistry.newCommandLineProcessLogging())
                    .provider(new GlobalScopeServices(false))
                    .build();
        }
    }


    private static class Result implements BuildLayoutResult {
        private final BuildLayoutParameters buildLayout;

        public Result(BuildLayoutParameters buildLayout) {
            this.buildLayout = buildLayout;
        }

        @Override
        public void applyTo(BuildLayoutParameters buildLayout) {
            buildLayout.setCurrentDir(this.buildLayout.getCurrentDir());
            buildLayout.setProjectDir(this.buildLayout.getProjectDir());
            buildLayout.setGradleUserHomeDir(this.buildLayout.getGradleUserHomeDir());
            buildLayout.setGradleInstallationHomeDir(this.buildLayout.getGradleInstallationHomeDir());
        }

        @Override
        public void applyTo(StartParameterInternal startParameter) {
            startParameter.setProjectDir(buildLayout.getProjectDir());
            startParameter.setCurrentDir(buildLayout.getCurrentDir());
            startParameter.setGradleUserHomeDir(buildLayout.getGradleUserHomeDir());
        }

        @Override
        public File getGradleUserHomeDir() {
            return buildLayout.getGradleUserHomeDir();
        }
    }

    private BuildActionParameters createBuildActionParameters(StartParameter startParameter) {
        return new DefaultBuildActionParameters(
//                daemonParameters.getEffectiveSystemProperties(),
                Collections.emptyMap(),
//                daemonParameters.getEnvironmentVariables(),
                Collections.emptyMap(),
                SystemProperties.getInstance().getCurrentDir(),
                startParameter.getLogLevel(),
//                daemonParameters.isEnabled(),
                false,
                ClassPath.EMPTY);
    }


    public ServiceRegistry getGlobalServices() {
        return globalServices;
    }

    private long getBuildStartTime() {
        return System.currentTimeMillis();
    }

    private GradleLauncherMetaData clientMetaData() {
        return new GradleLauncherMetaData();
    }

    private Runnable runBuildAndCloseServices(StartParameterInternal startParameter, BuildActionExecuter<BuildActionParameters, BuildRequestContext> executer, ServiceRegistry sharedServices, Object... stopBeforeSharedServices) {
        BuildActionParameters
                parameters = createBuildActionParameters(startParameter);
        Stoppable stoppable = new CompositeStoppable(); //.add(stopBeforeSharedServices).add(sharedServices);
        return new RunBuildAction(executer, startParameter, clientMetaData(), getBuildStartTime(), parameters, sharedServices, stoppable);
    }


    private void prepare() {
        GradleUserHomeScopeServiceRegistry gradleUserHomeScopeServiceRegistry =
                globalServices.get(GradleUserHomeScopeServiceRegistry.class);
        ServiceRegistry gradleUserHomeScopeServices = gradleUserHomeScopeServiceRegistry
                .getServicesFor(startParameter.getGradleUserHomeDir());
        VirtualFileSystem virtualFileSystem =
                gradleUserHomeScopeServices.get(VirtualFileSystem.class);
        FileSystemAccess fileSystemAccess = gradleUserHomeScopeServices.get(FileSystemAccess.class);
    }


    public void onCreateGradle(Action<Gradle> action) {
        globalServices
                .get(ListenerManager.class)
                .addListener(new BuildAdapter() {
                    @Override
                    public void projectsLoaded(Gradle gradle) {
                        action.execute(gradle);
                    }
                });
    }

    public void execute() {

        Runnable runnable = runBuildAndCloseServices(
                startParameter,
                globalServices.get(BuildExecuter.class),
                globalServices,
                globalServices.get(GradleUserHomeScopeServiceRegistry.class)
        );
        Action<Throwable> reporter = throwable -> {

        };

        LoggingConfiguration loggingConfiguration = new DefaultLoggingConfiguration();


        LoggingManagerInternal loggingManagerInternal =
                globalServices.get(LoggingManagerInternal.class);

        BuildLayoutParameters layoutParameters = new BuildLayoutParameters();
        layoutParameters.setCurrentDir(SystemProperties.getInstance().getCurrentDir());
        layoutParameters.setProjectDir(startParameter.getProjectDir());
        layoutParameters.setGradleUserHomeDir(startParameter.getGradleUserHomeDir());

        BuildLayoutResult buildLayout = new Result(layoutParameters);

        Action<ExecutionListener> exceptionReportingAction =
                new ExceptionReportingAction(reporter, loggingManagerInternal,
                        new NativeServicesInitializingAction(buildLayout, loggingConfiguration, loggingManagerInternal,
                                new WelcomeMessageAction(buildLayout,
                                        new DebugLoggerWarningAction(loggingConfiguration, listener -> {
                                            runnable.run();
                                        }))));

        prepare();
        exceptionReportingAction.execute(failure -> {

            if (!(failure instanceof ReportedException)) {
                throw UncheckedException.throwAsUncheckedException(failure);
            }
        });


    }


    public synchronized static TestGradleLauncher createLauncher(Action<StartParameter> configAction) {
        StartParameterInternal startParameterInternal = new StartParameterInternal();
        configAction.execute(startParameterInternal);
        return new TestGradleLauncher(startParameterInternal);
    }

}
