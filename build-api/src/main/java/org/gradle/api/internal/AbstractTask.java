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
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.internal.tasks.InputChangesAwareTaskAction;
import org.gradle.api.internal.tasks.TaskDependencyInternal;
import org.gradle.api.internal.tasks.TaskStateInternal;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskDestroyables;
import org.gradle.api.tasks.TaskLocalState;

import java.io.File;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * @deprecated This class will be removed in Gradle 8.0. Please use {@link org.gradle.api.DefaultTask} instead.
 */
@Deprecated
public abstract class AbstractTask implements TaskInternal /*, DynamicObjectAware */ {

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Project getProject() {
        return null;
    }

    @Override
    public List<Action<? super Task>> getActions() {
        return null;
    }

    @Override
    public void setActions(List<Action<? super Task>> actions) {

    }

    @Override
    public Set<Object> getDependsOn() {
        return null;
    }

    @Override
    public void setDependsOn(Iterable<?> dependsOnTasks) {

    }

    @Override
    public Task dependsOn(Object... paths) {
        return null;
    }

    @Override
    public TaskDestroyables getDestroyables() {
        return null;
    }

    @Override
    public TaskLocalState getLocalState() {
        return null;
    }

    @Override
    public File getTemporaryDir() {
        return null;
    }

    @Override
    public void onlyIf(Spec<? super Task> onlyIfSpec) {

    }

    @Override
    public void setOnlyIf(Spec<? super Task> onlyIfSpec) {

    }

    @Override
    public void setDidWork(boolean didWork) {

    }

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public Task doFirst(Action<? super Task> action) {
        return null;
    }

    @Override
    public Task doFirst(String actionName, Action<? super Task> action) {
        return null;
    }

    @Override
    public Task doLast(Action<? super Task> action) {
        return null;
    }

    @Override
    public Task doLast(String actionName, Action<? super Task> action) {
        return null;
    }

    @Override
    public boolean getEnabled() {
        return false;
    }

    @Override
    public void setEnabled(boolean enabled) {

    }

    @Override
    public Task configure(Action<? extends Task> configureClosure) {
        return null;
    }

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void setDescription(@Nullable String description) {

    }

    @Nullable
    @Override
    public String getGroup() {
        return null;
    }

    @Override
    public void setGroup(@Nullable String group) {

    }

    @Override
    public Task mustRunAfter(Object... paths) {
        return null;
    }

    @Override
    public void setMustRunAfter(Iterable<?> mustRunAfter) {

    }

    @Override
    public TaskDependency getMustRunAfter() {
        return null;
    }

    @Override
    public Task finalizedBy(Object... paths) {
        return null;
    }

    @Override
    public void setFinalizedBy(Iterable<?> finalizedBy) {

    }

    @Override
    public TaskDependency getFinalizedBy() {
        return null;
    }

    @Override
    public TaskDependency shouldRunAfter(Object... paths) {
        return null;
    }

    @Override
    public void setShouldRunAfter(Iterable<?> shouldRunAfter) {

    }

    @Override
    public TaskDependency getShouldRunAfter() {
        return null;
    }


    @Override
    public boolean hasTaskActions() {
        return false;
    }

    @Override
    public Spec<? super TaskInternal> getOnlyIf() {
        return null;
    }

    @Override
    public TaskInputsInternal getInputs() {
        return null;
    }

    @Override
    public TaskOutputsInternal getOutputs() {
        return null;
    }

    @Override
    public TaskDependencyInternal getTaskDependencies() {
        return null;
    }


    @Override
    public TaskStateInternal getState() {
        return null;
    }

    @Override
    public boolean getImpliesSubProjects() {
        return false;
    }

    @Override
    public void setImpliesSubProjects(boolean impliesSubProjects) {

    }

    @Override
    public void prependParallelSafeAction(Action<? super Task> action) {

    }

    @Override
    public List<InputChangesAwareTaskAction> getTaskActions() {
        return null;
    }

    @Override
    public int compareTo(Task otherTask) {
       /* int depthCompare = project.compareTo(otherTask.getProject());
        if (depthCompare == 0) {
            return getPath().compareTo(otherTask.getPath());
        } else {
            return depthCompare;
        }

        */
        //TODO
        return 0;
    }

    @Override
    public void appendParallelSafeAction(Action<? super Task> action) {

    }

    @Override
    public boolean isHasCustomActions() {
        return false;
    }

    public boolean getDidWork() {
        //TODO Auto-generated method stub
        return false;
    }
}