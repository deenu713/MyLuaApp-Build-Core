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


import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;

import org.gradle.api.Action;
import org.gradle.api.Describable;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.internal.project.taskfactory.TaskIdentity;
import org.gradle.api.internal.tasks.DefaultTaskDependency;
import org.gradle.api.internal.tasks.InputChangesAwareTaskAction;
import org.gradle.api.internal.tasks.TaskContainerInternal;
import org.gradle.api.internal.tasks.TaskDependencyInternal;
import org.gradle.api.internal.tasks.TaskLocalStateInternal;
import org.gradle.api.internal.tasks.TaskMutator;
import org.gradle.api.internal.tasks.TaskStateInternal;
import org.gradle.api.internal.tasks.properties.PropertyWalker;
import org.gradle.api.provider.Property;
import org.gradle.api.specs.AndSpec;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskDestroyables;
import org.gradle.api.tasks.TaskInstantiationException;
import org.gradle.api.tasks.TaskLocalState;
import org.gradle.internal.Cast;
import org.gradle.internal.service.ServiceRegistry;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import groovy.util.ObservableList;

/**
 * @deprecated This class will be removed in Gradle 8.0. Please use {@link org.gradle.api.DefaultTask} instead.
 */
@Deprecated
public abstract class AbstractTask implements TaskInternal /*, DynamicObjectAware */ {

    private static final ThreadLocal<TaskInfo> NEXT_INSTANCE = new ThreadLocal<TaskInfo>();

    private final TaskIdentity<?> identity;

    private final ProjectInternal project;

    private List<InputChangesAwareTaskAction> actions;

    private boolean enabled = true;

    private final DefaultTaskDependency dependencies;

    private final DefaultTaskDependency mustRunAfter;

    private final DefaultTaskDependency finalizedBy;

    private final DefaultTaskDependency shouldRunAfter;


    private String description;

    private String group;

    //private final Property<Duration> timeout;

    private AndSpec<Task> onlyIfSpec = createNewOnlyIfSpec();


    private final TaskStateInternal state;

    private final ServiceRegistry services;


    private final TaskMutator taskMutator;
    private ObservableList observableActionList;
    private boolean impliesSubProjects;
    private boolean hasCustomActions;


    private final TaskInputsInternal taskInputs;
     /*
    private final TaskOutputsInternal taskOutputs;
    private final TaskDestroyables taskDestroyables;
    private final TaskLocalStateInternal taskLocalState;

     */


    protected AbstractTask() {
        this(taskInfo());
    }

    private static TaskInfo taskInfo() {
        return NEXT_INSTANCE.get();
    }

    private AbstractTask(TaskInfo taskInfo) {
        if (taskInfo == null) {
            throw new TaskInstantiationException(String.format("Task of type '%s' has been instantiated directly which is not supported. Tasks can only be created using the Gradle API or DSL.", getClass().getName()));
        }

        this.identity = taskInfo.identity;
        this.project = taskInfo.project;
        assert project != null;
        assert identity.name != null;
        this.state = new TaskStateInternal();
        TaskContainerInternal tasks = project.getTasks();
        this.mustRunAfter = new DefaultTaskDependency(tasks);
        this.finalizedBy = new DefaultTaskDependency(tasks);
        this.shouldRunAfter = new DefaultTaskDependency(tasks);
        this.services = project.getServices();

        PropertyWalker propertyWalker = services.get(PropertyWalker.class);
        FileCollectionFactory fileCollectionFactory = services.get(FileCollectionFactory.class);
        taskMutator = new TaskMutator(this);

        taskInputs = new DefaultTaskInputs(this, taskMutator, propertyWalker, fileCollectionFactory);
       /*
        taskOutputs = new DefaultTaskOutputs(this, taskMutator, propertyWalker, fileCollectionFactory);
        taskDestroyables = new DefaultTaskDestroyables(taskMutator, fileCollectionFactory);
        taskLocalState = new DefaultTaskLocalState(taskMutator, fileCollectionFactory);
        */

        this.dependencies = new DefaultTaskDependency(tasks, ImmutableSet.of(taskInputs));

        //this.timeout = project.getObjects().property(Duration.class);
    }


    private AndSpec<Task> createNewOnlyIfSpec() {
        return new AndSpec<Task>(new Spec<Task>() {
            @Override
            public boolean isSatisfiedBy(Task element) {
                return element == AbstractTask.this && enabled;
            }
        });
    }


    @Override
    public String getName() {
        return null;
    }

    @Override
    public Project getProject() {
        return null;
    }


    private InputChangesAwareTaskAction wrap(final Action<? super Task> action) {
        return wrap(action, "unnamed action");
    }

    private InputChangesAwareTaskAction wrap(final Action<? super Task> action, String actionName) {
        if (action instanceof InputChangesAwareTaskAction) {
            return (InputChangesAwareTaskAction) action;
        }
        return new TaskActionWrapper(action, actionName);
    }


    private static class TaskActionWrapper implements InputChangesAwareTaskAction {
        private final Action<? super Task> action;
        private final String maybeActionName;

        /**
         * The <i>action name</i> is used to construct a human readable name for
         * the actions to be used in progress logging. It is only used if
         * the wrapped action does not already implement {@link Describable}.
         */
        public TaskActionWrapper(Action<? super Task> action, String maybeActionName) {
            this.action = action;
            this.maybeActionName = maybeActionName;
        }

        /**
         * @Override public void setInputChanges(InputChangesInternal inputChanges) {
         * }
         */

        @Override
        public void clearInputChanges() {
        }

        @Override
        public void execute(Task task) {
            ClassLoader original = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(action.getClass().getClassLoader());
            try {
                action.execute(task);
            } finally {
                Thread.currentThread().setContextClassLoader(original);
            }
        }

        /*
        @Override
        public ImplementationSnapshot getActionImplementation(ClassLoaderHierarchyHasher hasher) {
            return ImplementationSnapshot.of(AbstractTask.getActionClassName(action), hasher.getClassLoaderHash(action.getClass().getClassLoader()));
        }

         */
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof TaskActionWrapper)) {
                return false;
            }

            TaskActionWrapper that = (TaskActionWrapper) o;
            return action.equals(that.action);
        }

        @Override
        public int hashCode() {
            return action.hashCode();
        }

        @Override
        public String getDisplayName() {
            if (action instanceof Describable) {
                return ((Describable) action).getDisplayName();
            }
            return "Execute " + maybeActionName;
        }
    }

    private class ObservableActionWrapperList extends ObservableList {
        public ObservableActionWrapperList(List delegate) {
            super(delegate);
        }

        @Override
        public boolean add(Object action) {
            if (action == null) {
                throw new InvalidUserDataException("Action must not be null!");
            }
            return super.add(wrap(Cast.uncheckedNonnullCast(action)));
        }

        @Override
        public void add(int index, Object action) {
            if (action == null) {
                throw new InvalidUserDataException("Action must not be null!");
            }
            super.add(index, wrap(Cast.uncheckedNonnullCast(action)));
        }

        @Override
        public boolean addAll(Collection actions) {
            if (actions == null) {
                throw new InvalidUserDataException("Actions must not be null!");
            }
            return super.addAll(transformToContextAwareTaskActions(Cast.uncheckedNonnullCast(actions)));
        }

        @Override
        public boolean addAll(int index, Collection actions) {
            if (actions == null) {
                throw new InvalidUserDataException("Actions must not be null!");
            }
            return super.addAll(index, transformToContextAwareTaskActions(Cast.uncheckedNonnullCast(actions)));
        }

        @Override
        public Object set(int index, Object action) {
            if (action == null) {
                throw new InvalidUserDataException("Action must not be null!");
            }
            return super.set(index, wrap(Cast.uncheckedNonnullCast(action)));
        }

        @Override
        public boolean removeAll(Collection actions) {
            return super.removeAll(transformToContextAwareTaskActions(Cast.uncheckedNonnullCast(actions)));
        }

        @Override
        public boolean remove(Object action) {
            return super.remove(wrap(Cast.uncheckedNonnullCast(action)));
        }

        private Collection<InputChangesAwareTaskAction> transformToContextAwareTaskActions(Collection<Object> c) {
            return Collections2.transform(c, input -> wrap(Cast.uncheckedCast(input)));
        }
    }

    @Override
    public List<Action<? super Task>> getActions() {
        if (observableActionList == null) {
            observableActionList = new ObservableActionWrapperList(getTaskActions());
            observableActionList.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    taskMutator.assertMutable("Task.getActions()", evt);
                }
            });
        }
        return Cast.uncheckedNonnullCast(observableActionList);
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


    private static class TaskInfo {
        private final TaskIdentity<?> identity;
        private final ProjectInternal project;

        private TaskInfo(TaskIdentity<?> identity, ProjectInternal project) {
            this.identity = identity;
            this.project = project;
        }
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