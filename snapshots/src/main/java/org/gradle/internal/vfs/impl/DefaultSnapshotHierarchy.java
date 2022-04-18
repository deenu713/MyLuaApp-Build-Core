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

package org.gradle.internal.vfs.impl;

import com.google.common.annotations.VisibleForTesting;
import org.gradle.internal.file.FileType;
import org.gradle.internal.snapshot.CaseSensitivity;
import org.gradle.internal.snapshot.FileSystemNode;
import org.gradle.internal.snapshot.MetadataSnapshot;
import org.gradle.internal.snapshot.PartialDirectoryNode;
import org.gradle.internal.snapshot.ReadOnlyFileSystemNode;
import org.gradle.internal.snapshot.SingletonChildMap;
import org.gradle.internal.snapshot.SnapshotHierarchy;
import org.gradle.internal.snapshot.UnknownFileSystemNode;
import org.gradle.internal.snapshot.VfsRelativePath;

import java.util.Optional;

public class DefaultSnapshotHierarchy implements SnapshotHierarchy {

    private final CaseSensitivity caseSensitivity;
    @VisibleForTesting
    final FileSystemNode rootNode;

    public static SnapshotHierarchy from(FileSystemNode rootNode, CaseSensitivity caseSensitivity) {
        return new DefaultSnapshotHierarchy(rootNode, caseSensitivity);
    }

    private DefaultSnapshotHierarchy(FileSystemNode rootNode, CaseSensitivity caseSensitivity) {
        this.caseSensitivity = caseSensitivity;
        this.rootNode = rootNode;
    }

    public static SnapshotHierarchy empty(CaseSensitivity caseSensitivity) {
        switch (caseSensitivity) {
            case CASE_SENSITIVE:
                return EmptySnapshotHierarchy.CASE_SENSITIVE;
            case CASE_INSENSITIVE:
                return EmptySnapshotHierarchy.CASE_INSENSITIVE;
            default:
                throw new AssertionError("Unknown case sensitivity: " + caseSensitivity);
        }
    }

    @Override
    public Optional<MetadataSnapshot> getMetadata(String absolutePath) {
        VfsRelativePath relativePath = VfsRelativePath.of(absolutePath);
        if (relativePath.length() == 0) {
            return rootNode.getSnapshot();
        }
        return rootNode.getSnapshot(relativePath, caseSensitivity);
    }

    @Override
    public boolean hasDescendantsUnder(String absolutePath) {
        return getNode(absolutePath).hasDescendants();
    }

    @Override
    public SnapshotHierarchy store(String absolutePath, MetadataSnapshot snapshot, NodeDiffListener diffListener) {
        VfsRelativePath relativePath = VfsRelativePath.of(absolutePath);
        if (relativePath.length() == 0) {
            return new DefaultSnapshotHierarchy(snapshot.asFileSystemNode(), caseSensitivity);
        }
        return new DefaultSnapshotHierarchy(
            rootNode.store(relativePath, caseSensitivity, snapshot, diffListener),
            caseSensitivity
        );
    }

    @Override
    public SnapshotHierarchy invalidate(String absolutePath, NodeDiffListener diffListener) {
        VfsRelativePath relativePath = VfsRelativePath.of(absolutePath);
        if (relativePath.length() == 0) {
            diffListener.nodeRemoved(rootNode);
            return empty();
        }
        return rootNode.invalidate(relativePath, caseSensitivity, diffListener)
            .<SnapshotHierarchy>map(it -> (it == rootNode) ? this : new DefaultSnapshotHierarchy(it, caseSensitivity))
            .orElseGet(() -> empty(caseSensitivity));
    }

    @Override
    public SnapshotHierarchy empty() {
        return empty(caseSensitivity);
    }

    @Override
    public void visitSnapshotRoots(SnapshotVisitor snapshotVisitor) {
        rootNode.accept(snapshotVisitor);
    }

    @Override
    public void visitSnapshotRoots(String absolutePath, SnapshotVisitor snapshotVisitor) {
        getNode(absolutePath).accept(snapshotVisitor);
    }

    private ReadOnlyFileSystemNode getNode(String absolutePath) {
        VfsRelativePath relativePath = VfsRelativePath.of(absolutePath);
        return (relativePath.length() == 0) ? rootNode : rootNode.getNode(relativePath, caseSensitivity);
    }

    private enum EmptySnapshotHierarchy implements SnapshotHierarchy {
        CASE_SENSITIVE(CaseSensitivity.CASE_SENSITIVE),
        CASE_INSENSITIVE(CaseSensitivity.CASE_INSENSITIVE);

        private final CaseSensitivity caseSensitivity;

        EmptySnapshotHierarchy(CaseSensitivity caseInsensitive) {
            this.caseSensitivity = caseInsensitive;
        }

        @Override
        public Optional<MetadataSnapshot> getMetadata(String absolutePath) {
            return Optional.empty();
        }

        @Override
        public boolean hasDescendantsUnder(String absolutePath) {
            return false;
        }

        @Override
        public SnapshotHierarchy store(String absolutePath, MetadataSnapshot snapshot, NodeDiffListener diffListener) {
            VfsRelativePath relativePath = VfsRelativePath.of(absolutePath);
            String childPath = relativePath.getAsString();
            SingletonChildMap<FileSystemNode> children = new SingletonChildMap<>(childPath, snapshot.asFileSystemNode());
            FileSystemNode rootNode = snapshot.getType() == FileType.Missing
                ? new UnknownFileSystemNode(children)
                : new PartialDirectoryNode(children);
            diffListener.nodeAdded(rootNode);
            return from(rootNode, caseSensitivity);
        }

        @Override
        public SnapshotHierarchy invalidate(String absolutePath, NodeDiffListener diffListener) {
            return this;
        }

        @Override
        public SnapshotHierarchy empty() {
            return this;
        }

        @Override
        public void visitSnapshotRoots(SnapshotVisitor snapshotVisitor) {}

        @Override
        public void visitSnapshotRoots(String absolutePath, SnapshotVisitor snapshotVisitor) {}
    }
}
