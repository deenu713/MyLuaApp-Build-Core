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
package org.gradle.api.internal.artifacts.ivyservice.ivyresolve;

import com.google.common.io.Files;
import org.gradle.api.artifacts.ArtifactIdentifier;
import org.gradle.api.artifacts.ComponentMetadataSupplierDetails;
import org.gradle.api.artifacts.component.ComponentArtifactIdentifier;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.internal.artifacts.DefaultArtifactIdentifier;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.artifact.ResolvableArtifact;
import org.gradle.api.internal.artifacts.repositories.metadata.DefaultMetadataFileSource;
import org.gradle.api.internal.artifacts.repositories.resolver.MetadataFetchingCost;
import org.gradle.api.internal.component.ArtifactType;
import org.gradle.api.internal.tasks.DefaultTaskDependency;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.internal.action.InstantiatingAction;
import org.gradle.internal.component.external.model.DefaultModuleComponentArtifactIdentifier;
import org.gradle.internal.component.external.model.ModuleComponentArtifactIdentifier;
import org.gradle.internal.component.external.model.ModuleComponentArtifactMetadata;
import org.gradle.internal.component.external.model.ModuleDependencyMetadata;
import org.gradle.internal.component.model.ComponentArtifactMetadata;
import org.gradle.internal.component.model.ComponentOverrideMetadata;
import org.gradle.internal.component.model.ComponentResolveMetadata;
import org.gradle.internal.component.model.ConfigurationMetadata;
import org.gradle.internal.component.model.DefaultIvyArtifactName;
import org.gradle.internal.component.model.IvyArtifactName;
import org.gradle.internal.component.model.ModuleDescriptorArtifactMetadata;
import org.gradle.internal.component.model.ModuleSources;
import org.gradle.internal.resolve.result.BuildableArtifactResolveResult;
import org.gradle.internal.resolve.result.BuildableArtifactSetResolveResult;
import org.gradle.internal.resolve.result.BuildableComponentArtifactsResolveResult;
import org.gradle.internal.resolve.result.BuildableModuleComponentMetaDataResolveResult;
import org.gradle.internal.resolve.result.BuildableModuleVersionListingResolveResult;
import org.gradle.internal.resolve.result.DefaultBuildableArtifactResolveResult;
import org.gradle.internal.resolve.result.DefaultBuildableModuleComponentMetaDataResolveResult;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class DependencyVerifyingModuleComponentRepository implements ModuleComponentRepository {
    private final ModuleComponentRepository delegate;
    private final ModuleComponentRepositoryAccess localAccess;
    private final ModuleComponentRepositoryAccess remoteAccess;


    public DependencyVerifyingModuleComponentRepository(ModuleComponentRepository delegate, boolean verifySignatures) {
        this.delegate = delegate;
        this.localAccess = new VerifyingModuleComponentRepositoryAccess(delegate.getLocalAccess(), verifySignatures);
        this.remoteAccess = new VerifyingModuleComponentRepositoryAccess(delegate.getRemoteAccess(), verifySignatures);

    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public ModuleComponentRepositoryAccess getLocalAccess() {
        return localAccess;
    }

    @Override
    public ModuleComponentRepositoryAccess getRemoteAccess() {
        return remoteAccess;
    }

    @Override
    public Map<ComponentArtifactIdentifier, ResolvableArtifact> getArtifactCache() {
        return delegate.getArtifactCache();
    }

    @Override
    @Nullable
    public InstantiatingAction<ComponentMetadataSupplierDetails> getComponentMetadataSupplier() {
        return delegate.getComponentMetadataSupplier();
    }

    private class VerifyingModuleComponentRepositoryAccess implements ModuleComponentRepositoryAccess {
        private final ModuleComponentRepositoryAccess delegate;
        private final boolean verifySignatures;

        private VerifyingModuleComponentRepositoryAccess(ModuleComponentRepositoryAccess delegate, boolean verifySignatures) {
            this.delegate = delegate;
            this.verifySignatures = verifySignatures;
        }

        @Override
        public void listModuleVersions(ModuleDependencyMetadata dependency, BuildableModuleVersionListingResolveResult result) {
            delegate.listModuleVersions(dependency, result);
        }

        private boolean hasUsableResult(BuildableModuleComponentMetaDataResolveResult result) {
            return result.hasResult() && result.getState() == BuildableModuleComponentMetaDataResolveResult.State.Resolved;
        }

        @Override
        public void resolveComponentMetaData(ModuleComponentIdentifier moduleComponentIdentifier, ComponentOverrideMetadata requestMetaData, BuildableModuleComponentMetaDataResolveResult result) {
            // For metadata, because the local file can be deleted we have to proceed in two steps
            // First resolve with a tmp result, and if it's found and that the file is still present
            // we can perform verification. If it's missing, then we do nothing so that it's downloaded
            // and verified later.
            BuildableModuleComponentMetaDataResolveResult tmp = new DefaultBuildableModuleComponentMetaDataResolveResult();
            delegate.resolveComponentMetaData(moduleComponentIdentifier, requestMetaData, tmp);
            AtomicBoolean ignore = new AtomicBoolean();
            if (hasUsableResult(tmp)) {
                tmp.getMetaData().getSources().withSources(DefaultMetadataFileSource.class, metadataFileSource -> {
                    ModuleComponentArtifactIdentifier artifact = metadataFileSource.getArtifactId();
                    if (isExternalArtifactId(artifact)) {
                        tmp.getMetaData().getSources().withSource(ModuleDescriptorHashModuleSource.class, hashSource -> {
                            if (hashSource.isPresent()) {
                                boolean changingModule = requestMetaData.isChanging() || hashSource.get().isChangingModule();
                                if (!changingModule) {
                                    File artifactFile = metadataFileSource.getArtifactFile();
                                   /* if (artifactFile != null && artifactFile.exists()) {
                                        // it's possible that the file is null if it has been removed from the cache
                                        // for example
                                        operation.onArtifact(ArtifactVerificationOperation.ArtifactKind.METADATA, artifact, artifactFile, () -> maybeFetchSignatureFile(moduleComponentIdentifier, tmp.getMetaData().getSources(), artifact), getName(), getId());
                                    } else {*/
                                        ignore.set(true);
                                    /*}*/
                                }
                            }
                            return null;
                        });
                    }
                });
            }

            if (!ignore.get()) {
                delegate.resolveComponentMetaData(moduleComponentIdentifier, requestMetaData, result);
            }
        }

        private File maybeFetchSignatureFile(ModuleComponentIdentifier moduleComponentIdentifier, ModuleSources moduleSources, ModuleComponentArtifactIdentifier artifact) {
            if (!verifySignatures) {
                return null;
            }
            SignatureFileDefaultBuildableArtifactResolveResult signatureResult = new SignatureFileDefaultBuildableArtifactResolveResult();
            SignatureArtifactMetadata signatureArtifactMetadata = new SignatureArtifactMetadata(moduleComponentIdentifier, artifact);
            getLocalAccess().resolveArtifact(signatureArtifactMetadata, moduleSources, signatureResult);
            if (signatureResult.hasResult()) {
                if (signatureResult.isSuccessful()) {
                    return signatureResult.getResult();
                }
                return null;
            } else {
                getRemoteAccess().resolveArtifact(signatureArtifactMetadata, moduleSources, signatureResult);
            }
            if (signatureResult.hasResult() && signatureResult.isSuccessful()) {
                return signatureResult.getResult();
            }
            return null;
        }

        @Override
        public void resolveArtifacts(ComponentResolveMetadata component, ConfigurationMetadata variant, BuildableComponentArtifactsResolveResult result) {
            delegate.resolveArtifacts(component, variant, result);
        }

        @Override
        public void resolveArtifactsWithType(ComponentResolveMetadata component, ArtifactType artifactType, BuildableArtifactSetResolveResult result) {
            delegate.resolveArtifactsWithType(component, artifactType, result);
        }

        @Override
        public void resolveArtifact(ComponentArtifactMetadata artifact, ModuleSources moduleSources, BuildableArtifactResolveResult result) {
            delegate.resolveArtifact(artifact, moduleSources, result);
            if (result.hasResult() && result.isSuccessful()) {
                ComponentArtifactIdentifier id = artifact.getId();
                if (isExternalArtifactId(id) && isNotChanging(moduleSources)) {
                  /*  ModuleComponentArtifactIdentifier mcai = (ModuleComponentArtifactIdentifier) id;
                    ArtifactVerificationOperation.ArtifactKind artifactKind = determineArtifactKind(artifact);
                    if (!(result instanceof SignatureFileDefaultBuildableArtifactResolveResult)) {
                        // signature files are fetched using resolveArtifact, but are checked alongside the main artifact
                        operation.onArtifact(artifactKind, mcai, result.getResult(), () -> maybeFetchSignatureFile(((ModuleComponentArtifactIdentifier) id).getComponentIdentifier(), moduleSources, mcai), getName(), getId());
                    }*/
                }
            }
        }


        private boolean isNotChanging(ModuleSources moduleSources) {
            return moduleSources.withSource(ModuleDescriptorHashModuleSource.class, source ->
                source.map(cachingModuleSource -> !cachingModuleSource.isChangingModule()).orElse(true));
        }

        private boolean isExternalArtifactId(ComponentArtifactIdentifier id) {
            return id instanceof ModuleComponentArtifactIdentifier;
        }

        @Override
        public MetadataFetchingCost estimateMetadataFetchingCost(ModuleComponentIdentifier moduleComponentIdentifier) {
            return delegate.estimateMetadataFetchingCost(moduleComponentIdentifier);
        }

        private class SignatureArtifactMetadata implements ModuleComponentArtifactMetadata {

            private final ModuleComponentIdentifier moduleComponentIdentifier;
            private final ModuleComponentArtifactIdentifier artifactIdentifier;

            public SignatureArtifactMetadata(ModuleComponentIdentifier moduleComponentIdentifier, ModuleComponentArtifactIdentifier artifact) {
                this.moduleComponentIdentifier = moduleComponentIdentifier;
                this.artifactIdentifier = artifact.getSignatureArtifactId();
            }


            @Override
            public ModuleComponentArtifactIdentifier getId() {
                return artifactIdentifier;
            }

            @Override
            public ComponentIdentifier getComponentId() {
                return moduleComponentIdentifier;
            }

            @Override
            public IvyArtifactName getName() {
                if (artifactIdentifier instanceof DefaultModuleComponentArtifactIdentifier) {
                    return ((DefaultModuleComponentArtifactIdentifier) artifactIdentifier).getName();
                }
                // This is a bit hackish but the mapping from file names to ivy artifact names is completely broken
                String fileName = artifactIdentifier.getFileName().replace("-" + artifactIdentifier.getComponentIdentifier().getVersion(), "");
                fileName = Files.getNameWithoutExtension(fileName); // removes the .asc
                DefaultIvyArtifactName base = DefaultIvyArtifactName.forFileName(fileName, null);
                return new DefaultIvyArtifactName(
                    base.getName(),
                    "asc",
                    base.getExtension() + ".asc"
                );
            }

            @Override
            public TaskDependency getBuildDependencies() {
                return new DefaultTaskDependency();
            }

            @Override
            public ArtifactIdentifier toArtifactIdentifier() {
                return new DefaultArtifactIdentifier(
                    new DefaultModuleComponentArtifactIdentifier(
                        moduleComponentIdentifier, getName()
                    )
                );
            }

        }
    }

    private static class SignatureFileDefaultBuildableArtifactResolveResult extends DefaultBuildableArtifactResolveResult {
    }
}
