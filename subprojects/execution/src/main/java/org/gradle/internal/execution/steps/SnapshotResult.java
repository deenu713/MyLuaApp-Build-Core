/*
 * Copyright 2018 the original author or authors.
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

package org.gradle.internal.execution.steps;

import com.google.common.collect.ImmutableSortedMap;
import org.gradle.internal.snapshot.FileSystemSnapshot;

public interface SnapshotResult extends Result {
    /**
     * Snapshots of the roots of output properties.
     *
     * In the presence of overlapping outputs this might be different from
     * {@link BeforeExecutionState#getOutputFileLocationSnapshots()},
     * as this does not include overlapping outputs <em>not</em> produced by the work.
     */
    ImmutableSortedMap<String, FileSystemSnapshot> getOutputFilesProduceByWork();
}
