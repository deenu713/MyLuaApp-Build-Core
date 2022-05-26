/*
 * Copyright 2017 the original author or authors.
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

package org.gradle.api.internal.changedetection.state;

import org.gradle.cache.internal.CacheScopeMapping;
import org.gradle.cache.internal.VersionStrategy;
import org.gradle.initialization.RootBuildLifecycleListener;
import org.gradle.internal.service.scopes.Scopes;
import org.gradle.internal.service.scopes.ServiceScope;

import java.util.HashSet;
import java.util.Set;

/**
 * Used for the Gradle user home file hash cache.
 *
 * Uses the same strategy for detection of file changes as {@link FileTimeStampInspector}.
 *
 * Discards hashes for all files from the {@link CachingFileHasher} which have been queried on this daemon
 * during the last build and which have a timestamp equal to the end of build timestamp.
 */
@ServiceScope(Scopes.UserHome.class)
public class GradleUserHomeScopeFileTimeStampInspector extends FileTimeStampInspector implements RootBuildLifecycleListener {
    private CachingFileHasher fileHasher;
    private final Object lock = new Object();
    private long currentTimestamp;
    private final Set<String> filesWithCurrentTimestamp = new HashSet<>();

    public GradleUserHomeScopeFileTimeStampInspector(CacheScopeMapping cacheScopeMapping) {
        super(cacheScopeMapping.getBaseDirectory(null, "file-changes", VersionStrategy.CachePerVersion));
    }

    public void attach(CachingFileHasher fileHasher) {
        this.fileHasher = fileHasher;
    }

    @Override
    public void afterStart() {
        updateOnStartBuild();
        currentTimestamp = currentTimestamp();
    }

    @Override
    public boolean timestampCanBeUsedToDetectFileChange(String file, long timestamp) {
        synchronized (lock) {
            if (timestamp == currentTimestamp) {
                filesWithCurrentTimestamp.add(file);
            } else if (timestamp > currentTimestamp) {
                filesWithCurrentTimestamp.clear();
                filesWithCurrentTimestamp.add(file);
                currentTimestamp = timestamp;
            }
        }

        return super.timestampCanBeUsedToDetectFileChange(file, timestamp);
    }

    @Override
    public void beforeComplete() {
        updateOnFinishBuild();
        synchronized (lock) {
            try {
                // These files have an unreliable timestamp - discard any cached state for them and rehash next time they are seen
                if (currentTimestamp == getLastBuildTimestamp()) {
                    for (String path : filesWithCurrentTimestamp) {
                        fileHasher.discard(path);
                    }
                }
            } finally {
                filesWithCurrentTimestamp.clear();
            }
        }
    }
}
