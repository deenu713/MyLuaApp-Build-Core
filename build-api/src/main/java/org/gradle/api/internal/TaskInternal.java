/*
 * Copyright 2010 the original author or authors.
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

package org.gradle.api.internal;

import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.internal.tasks.InputChangesAwareTaskAction;
import org.gradle.api.internal.tasks.TaskStateInternal;

import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.Internal;


import java.util.List;

public interface TaskInternal extends Task {




    /**
     * A more efficient version of {@link #getActions()}, which circumvents the
     * validating change listener that normally prevents users from changing tasks
     * once they start executing.
     */
    @Internal
    List<InputChangesAwareTaskAction> getTaskActions();

    @Internal
    boolean hasTaskActions();

    @Internal
    Spec<? super TaskInternal> getOnlyIf();

    @Internal
    //StandardOutputCapture getStandardOutputCapture();

    @Override
    TaskInputsInternal getInputs();

    @Override
    TaskOutputsInternal getOutputs();

    @Override
    TaskStateInternal getState();

    @Internal
    boolean getImpliesSubProjects();

    void setImpliesSubProjects(boolean impliesSubProjects);

    /**
     * The returned factory is expected to return the same file each time.
     * <p>
     * The getTemporaryDir() method creates the directory which can be problematic. Use this to delay that creation.
     */
    @Internal
    //Factory<File> getTemporaryDirFactory();

    void prependParallelSafeAction(Action<? super Task> action);

    void appendParallelSafeAction(Action<? super Task> action);

    @Internal
    boolean isHasCustomActions();


    //Path getIdentityPath();


    //TaskIdentity<?> getTaskIdentity();


    //Set<Provider<? extends BuildService<?>>> getRequiredServices();

    /**
     * <p>Gets the shared resources required by this task.</p>
     */

    //List<? extends ResourceLock> getSharedResources();
}
