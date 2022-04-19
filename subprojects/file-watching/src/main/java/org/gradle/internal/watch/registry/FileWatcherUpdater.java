/*
 * Copyright 2020 the original author or authors.
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

package org.gradle.internal.watch.registry;

import org.gradle.internal.snapshot.FileSystemLocationSnapshot;
import org.gradle.internal.snapshot.SnapshotHierarchy;
import org.gradle.internal.watch.vfs.WatchMode;

import javax.annotation.CheckReturnValue;
import java.io.File;
import java.nio.file.Path;
import java.util.Collection;

public interface FileWatcherUpdater {
    /**
     * Registers a watchable hierarchy.
     *
     * @see FileWatcherRegistry#registerWatchableHierarchy(File, SnapshotHierarchy)
     */
    void registerWatchableHierarchy(File watchableHierarchy, SnapshotHierarchy root);

    /**
     * Updates the watchers after changes to the root.
     *
     * @see FileWatcherRegistry#virtualFileSystemContentsChanged(Collection, Collection, SnapshotHierarchy)
     */
    void virtualFileSystemContentsChanged(Collection<FileSystemLocationSnapshot> removedSnapshots, Collection<FileSystemLocationSnapshot> addedSnapshots, SnapshotHierarchy root);

    /**
     * Remove watched hierarchies that have been moved.
     *
     * @see FileWatcherRegistry#buildStarted(SnapshotHierarchy)
     */
    @CheckReturnValue
    SnapshotHierarchy buildStarted(SnapshotHierarchy root);

    /**
     * Remove everything from the root which can't be kept after the current build finished.
     *
     * @see FileWatcherRegistry#buildFinished(SnapshotHierarchy, WatchMode, int)
     */
    @CheckReturnValue
    SnapshotHierarchy buildFinished(SnapshotHierarchy root, WatchMode watchMode, int maximumNumberOfWatchedHierarchies);

    Collection<Path> getWatchedHierarchies();
}
