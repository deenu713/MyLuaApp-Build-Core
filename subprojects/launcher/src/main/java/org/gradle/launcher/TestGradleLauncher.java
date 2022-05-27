package org.gradle.launcher;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;

import org.gradle.BuildListener;
import org.gradle.BuildResult;
import org.gradle.StartParameter;
import org.gradle.api.Action;
import org.gradle.api.initialization.Settings;
import org.gradle.api.internal.StartParameterInternal;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.logging.LogLevel;
import org.gradle.configuration.GradleLauncherMetaData;
import org.gradle.initialization.BuildCancellationToken;
import org.gradle.initialization.BuildClientMetaData;
import org.gradle.initialization.BuildEventConsumer;
import org.gradle.initialization.BuildRequestContext;
import org.gradle.initialization.BuildRequestMetaData;
import org.gradle.initialization.DefaultBuildCancellationToken;
import org.gradle.initialization.DefaultBuildRequestContext;
import org.gradle.initialization.DefaultBuildRequestMetaData;
import org.gradle.internal.buildtree.BuildActionRunner;
import org.gradle.internal.classpath.ClassPath;
import org.gradle.internal.event.ListenerManager;
import org.gradle.internal.invocation.BuildAction;
import org.gradle.internal.logging.services.LoggingServiceRegistry;
import org.gradle.internal.nativeintegration.services.NativeServices;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.internal.service.ServiceRegistryBuilder;
import org.gradle.internal.service.scopes.GlobalScopeServices;
import org.gradle.internal.service.scopes.GradleUserHomeScopeServiceRegistry;
import org.gradle.internal.service.scopes.WorkerSharedGlobalScopeServices;
import org.gradle.internal.session.BuildSessionState;
import org.gradle.internal.session.CrossBuildSessionState;
import org.gradle.launcher.cli.ExecuteBuildAction;
import org.gradle.launcher.exec.BuildActionParameters;
import org.gradle.launcher.exec.BuildActionResult;
import org.gradle.launcher.exec.DefaultBuildActionParameters;
import org.gradle.testfixtures.internal.ProjectBuilderImpl;
import org.gradle.testfixtures.internal.TestGlobalScopeServices;

import java.io.File;
import java.util.concurrent.CompletableFuture;


public class TestGradleLauncher {


    private StartParameterInternal startParameter;

    private static ServiceRegistry globalServices;

    TestGradleLauncher(StartParameterInternal startParameter) {
        this.startParameter = startParameter;
        NativeServices.initializeOnClient(startParameter.getProjectDir());
        if (globalServices == null) {
            globalServices = ServiceRegistryBuilder
                    .builder()
                    .displayName("global services")
                    .parent(LoggingServiceRegistry.newCommandLineProcessLogging())
                    .parent(NativeServices.getInstance())
                    .provider(new GlobalScopeServices(false))
                    .build();
        }
    }


    public CompletableFuture<Gradle> create() {

        StartParameterInternal startParameter = this.startParameter;
        File projectDir = startParameter.getProjectDir();
        BuildCancellationToken cancellationToken = new DefaultBuildCancellationToken();
        BuildEventConsumer consumer = System.out::println;
        BuildRequestMetaData requestMetaData = new DefaultBuildRequestMetaData(
                System.currentTimeMillis()
        );
        BuildRequestContext requestContext = new DefaultBuildRequestContext(
                requestMetaData, cancellationToken, consumer
        );
        NativeServices.initializeOnClient(projectDir);
        BuildActionResult result = execute(new ExecuteBuildAction(startParameter),
                new DefaultBuildActionParameters(ImmutableMap.of(),
                        ImmutableMap.of(), projectDir, LogLevel.DEBUG,
                        false, ClassPath.EMPTY), requestContext);

        if (result.getFailure() != null) {
            throw (RuntimeException) result.getFailure();
        }

        return (CompletableFuture<Gradle>) result.getResult();

    }


    public BuildActionResult execute(BuildAction action, BuildActionParameters actionParameters, BuildRequestContext requestContext) {
        StartParameterInternal startParameter = action.getStartParameter();
        if (action.isCreateModel()) {
            // When creating a model, do not use continuous mode
            startParameter.setContinuous(false);
        }
        try (CrossBuildSessionState crossBuildSessionState = new CrossBuildSessionState(globalServices, startParameter)) {
            try (BuildSessionState buildSessionState = new BuildSessionState(
                    globalServices.get(GradleUserHomeScopeServiceRegistry.class),
                    crossBuildSessionState, startParameter,
                    requestContext, actionParameters.getInjectedPluginClasspath(),
                    requestContext.getCancellationToken(),
                    requestContext.getClient(), requestContext.getEventConsumer())) {
                return buildSessionState.run(context -> {
                    CompletableFuture<Gradle> future = CompletableFuture.completedFuture(null);
                    BuildActionRunner.Result result = context.execute(action);
                    ListenerManager listenerManager = globalServices
                            .get(ListenerManager.class);

                    listenerManager
                            .addListener(new BuildListener() {
                                @Override
                                public void settingsEvaluated(Settings settings) {
                                }

                                @Override
                                public void projectsLoaded(Gradle gradle) {
                                    future.complete(gradle);
                                }

                                @Override
                                public void projectsEvaluated(Gradle gradle) {
                                }

                                @Override
                                public void buildFinished(BuildResult result) {
                                }
                            });

                    if (result.getBuildFailure() == null) {
                        return BuildActionResult.of(future);
                    }

                    if (requestContext.getCancellationToken().isCancellationRequested()) {
                        return BuildActionResult.cancelled(result.getBuildFailure());
                    }
                    return BuildActionResult.failed(result.getClientFailure());
                });
            }
        }
    }


    public synchronized static TestGradleLauncher createLauncher(Action<StartParameter> configAction) {
        StartParameterInternal startParameterInternal = new StartParameterInternal();
        configAction.execute(startParameterInternal);
        return new TestGradleLauncher(startParameterInternal);
    }

}
