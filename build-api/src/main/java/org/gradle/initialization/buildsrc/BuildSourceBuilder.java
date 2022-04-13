/*
 * Copyright 2013 the original author or authors.
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

package org.gradle.initialization.buildsrc;

import org.gradle.StartParameter;
import org.gradle.api.internal.BuildDefinition;
import org.gradle.api.internal.GradleInternal;
import org.gradle.api.internal.SettingsInternal;
import org.gradle.api.internal.StartParameterInternal;
import org.gradle.api.internal.initialization.ClassLoaderScope;
import org.gradle.cache.FileLock;
import org.gradle.cache.FileLockManager;
import org.gradle.cache.LockOptions;
import org.gradle.internal.Actions;
import org.gradle.internal.build.BuildState;
import org.gradle.internal.build.BuildStateRegistry;
import org.gradle.internal.build.PublicBuildPath;
import org.gradle.internal.build.StandAloneNestedBuild;
import org.gradle.internal.classpath.CachedClasspathTransformer;
import org.gradle.internal.classpath.ClassPath;
import org.gradle.internal.operations.BuildOperationContext;
import org.gradle.internal.operations.BuildOperationDescriptor;
import org.gradle.internal.operations.BuildOperationExecutor;
import org.gradle.internal.operations.CallableBuildOperation;
import org.gradle.internal.service.scopes.Scopes;
import org.gradle.internal.service.scopes.ServiceScope;
import org.gradle.plugin.management.internal.PluginRequests;

import java.io.File;

import static org.gradle.api.internal.SettingsInternal.BUILD_SRC;
import static org.gradle.cache.internal.filelock.LockOptionsBuilder.mode;

@ServiceScope(Scopes.Build.class)
public class BuildSourceBuilder {
    private static final BuildBuildSrcBuildOperationType.Result BUILD_BUILDSRC_RESULT = new BuildBuildSrcBuildOperationType.Result() {
    };

    private final BuildState currentBuild;
    private final FileLockManager fileLockManager;
    private final BuildOperationExecutor buildOperationExecutor;
    private final CachedClasspathTransformer cachedClasspathTransformer;
    private final BuildSrcBuildListenerFactory buildSrcBuildListenerFactory;
    private final BuildStateRegistry buildRegistry;
    private final PublicBuildPath publicBuildPath;

    public BuildSourceBuilder(BuildState currentBuild, FileLockManager fileLockManager, BuildOperationExecutor buildOperationExecutor, CachedClasspathTransformer cachedClasspathTransformer, BuildSrcBuildListenerFactory buildSrcBuildListenerFactory, BuildStateRegistry buildRegistry, PublicBuildPath publicBuildPath) {
        this.currentBuild = currentBuild;
        this.fileLockManager = fileLockManager;
        this.buildOperationExecutor = buildOperationExecutor;
        this.cachedClasspathTransformer = cachedClasspathTransformer;
        this.buildSrcBuildListenerFactory = buildSrcBuildListenerFactory;
        this.buildRegistry = buildRegistry;
        this.publicBuildPath = publicBuildPath;
    }

    public ClassPath buildAndGetClassPath(GradleInternal gradle) {
        SettingsInternal settings = gradle.getSettings();
        File buildSrcDir = settings.getBuildSrcDir();
        ClassLoaderScope parentClassLoaderScope = settings.getClassLoaderScope();

        return createBuildSourceClasspath(buildSrcDir, gradle.getStartParameter(), parentClassLoaderScope);
    }

    private ClassPath createBuildSourceClasspath(File buildSrcDir, final StartParameter containingBuildParameters, ClassLoaderScope parentClassLoaderScope) {
        if (!BuildSrcDetector.isValidBuildSrcBuild(buildSrcDir)) {
            return ClassPath.EMPTY;
        }

        final StartParameterInternal buildSrcStartParameter = buildSrcStartParameterFor(buildSrcDir, containingBuildParameters);
        final BuildDefinition buildDefinition = BuildDefinition.fromStartParameterForBuild(
            buildSrcStartParameter,
            BUILD_SRC,
            buildSrcDir,
            PluginRequests.EMPTY,
            Actions.doNothing(),
            publicBuildPath,
            true
        );
        @SuppressWarnings("deprecation")
        File customBuildFile = buildSrcStartParameter.getBuildFile();
        assert customBuildFile == null;

        return buildOperationExecutor.call(new CallableBuildOperation<ClassPath>() {
            @Override
            public ClassPath call(BuildOperationContext context) {
                ClassPath classPath = buildBuildSrc(buildDefinition, parentClassLoaderScope);
                context.setResult(BUILD_BUILDSRC_RESULT);
                return classPath;
            }

            @Override
            public BuildOperationDescriptor.Builder description() {
                //noinspection Convert2Lambda
                return BuildOperationDescriptor.displayName("Build buildSrc").
                    progressDisplayName("Building buildSrc").
                    details(
                        new BuildBuildSrcBuildOperationType.Details() {
                            @Override
                            public String getBuildPath() {
                                return publicBuildPath.getBuildPath().toString();
                            }
                        }
                    );
            }
        });
    }

    private StartParameterInternal buildSrcStartParameterFor(File buildSrcDir, StartParameter containingBuildParameters) {
        final StartParameterInternal buildSrcStartParameter = (StartParameterInternal) containingBuildParameters.newBuild();
        buildSrcStartParameter.setCurrentDir(buildSrcDir);
        buildSrcStartParameter.setProjectProperties(containingBuildParameters.getProjectProperties());
        buildSrcStartParameter.doNotSearchUpwards();
        buildSrcStartParameter.setProfile(containingBuildParameters.isProfile());
        return buildSrcStartParameter;
    }

    @SuppressWarnings("try")
    private ClassPath buildBuildSrc(final BuildDefinition buildDefinition, ClassLoaderScope parentClassLoaderScope) {
        StandAloneNestedBuild nestedBuild = buildRegistry.addBuildSrcNestedBuild(buildDefinition, currentBuild);
        return nestedBuild.run(buildController -> {
            // Expose any contributions from the parent's settings
            buildController.getGradle().setClassLoaderScope(parentClassLoaderScope);

            try (FileLock ignored = buildSrcBuildLockFor(buildDefinition)) {
                return new BuildSrcUpdateFactory(buildController, buildSrcBuildListenerFactory, cachedClasspathTransformer).create();
            }
        });
    }

    private FileLock buildSrcBuildLockFor(BuildDefinition buildDefinition) {
        return fileLockManager.lock(
            new File(buildDefinition.getBuildRootDir(), ".gradle/noVersion/buildSrc"),
            LOCK_OPTIONS,
            "buildSrc build lock"
        );
    }

    private static final LockOptions LOCK_OPTIONS = mode(FileLockManager.LockMode.Exclusive).useCrossVersionImplementation();
}
