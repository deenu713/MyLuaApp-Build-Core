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
import org.gradle.api.internal.classpath.EffectiveClassPath;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.configuration.ConsoleOutput;
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
import org.gradle.initialization.SettingsEvaluatedCallbackFiringSettingsProcessor;
import org.gradle.internal.SystemProperties;
import org.gradle.internal.UncheckedException;
import org.gradle.internal.buildtree.BuildActionRunner;
import org.gradle.internal.classloader.ClassLoaderUtils;
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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;


public class TestGradleLauncher {


    private StartParameterInternal startParameter;

    private ServiceRegistry globalServices;

    private List<Class> injectPluginClasses;

    TestGradleLauncher(StartParameterInternal startParameter) {
        this.startParameter = startParameter;

        LoggingServiceRegistry serviceRegistry =
                LoggingServiceRegistry.newCommandLineProcessLogging();
        NativeServices.initializeOnDaemon(startParameter.getGradleUserHomeDir());
        globalServices = ServiceRegistryBuilder
                .builder()
                .parent(serviceRegistry)
                .parent(NativeServices.getInstance())
                .displayName("global services")
                .provider(new GlobalScopeServices(true, ClassPath.EMPTY))
                .build();

        injectPluginClasses = new ArrayList<>();
    }

    public void injectedPluginClasses(Class... classes) {
        this.injectPluginClasses.addAll(List.of(classes));
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
                startParameter.getCurrentDir(),
                startParameter.getLogLevel(),
//                daemonParameters.isEnabled(),
                false,
                injectPluginClasses
                        .stream().reduce(ClassPath.EMPTY,
                                (classPath, aClass) -> classPath.plus(new EffectiveClassPath(aClass.getClassLoader())),
                                ClassPath::plus)
        );
    }


    public ServiceRegistry getGlobalServices() {
        return globalServices;
    }

    public ServiceRegistry getGradleUserHomeServices() {
        return globalServices
                .get(GradleUserHomeScopeServiceRegistry.class)
                .getServicesFor(startParameter.getGradleUserHomeDir());
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
        ListenerManager listenerManager = getGradleUserHomeServices()
                .get(ListenerManager.class);
        System.out.println("listenerManager: " + listenerManager);
        listenerManager.addListener(new BuildAdapter() {
            @Override
            public void beforeSettings(Settings settings) {
                action.execute(settings.getGradle());
            }
        });
    }

    public String throwableToString(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }


    public void execute() {

        Runnable runnable = runBuildAndCloseServices(
                startParameter,
                globalServices.get(BuildExecuter.class),
                globalServices,
                globalServices.get(GradleUserHomeScopeServiceRegistry.class)
        );

        Action<Throwable> reporter = throwable -> {
            System.out.println(throwableToString(throwable));
        };

        LoggingConfiguration loggingConfiguration = new DefaultLoggingConfiguration();


        loggingConfiguration.setLogLevel(startParameter.getLogLevel());
        loggingConfiguration.setShowStacktrace(startParameter.getShowStacktrace());

        LoggingManagerInternal loggingManagerInternal =
                globalServices.get(LoggingManagerInternal.class);

        loggingManagerInternal
                .addStandardErrorListener(System.err);

        loggingManagerInternal
                .addStandardOutputListener(System.out);

        BuildLayoutParameters layoutParameters = new BuildLayoutParameters();
        layoutParameters.setCurrentDir(startParameter.getCurrentDir());
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
            System.err.println(throwableToString(failure));
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
