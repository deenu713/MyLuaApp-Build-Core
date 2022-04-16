package org.gradle.api;

import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.TaskOutputs;
import org.gradle.api.tasks.TaskState;

import java.util.List;

public interface Task {

    String TASK_NAME = "name";

    String TASK_DESCRIPTION = "description";

    String TASK_GROUP = "group";

    String TASK_TYPE = "type";

    String TASK_DEPENDS_ON = "dependsOn";

    String TASK_OVERWRITE = "overwrite";

    String TASK_ACTION = "action";

    /**
     * Constructor arguments for the Task
     *
     * @since 4.7
     */
    String TASK_CONSTRUCTOR_ARGS = "constructorArgs";



    /**
     * <p>Returns the name of this task. The name uniquely identifies the task within its {@link Project}.</p>
     *
     * @return The name of the task. Never returns null.
     */
    String getName();

    /**
     * A {@link org.gradle.api.Namer} namer for tasks that returns {@link #getName()}.
     */
    class Namer implements org.gradle.api.Namer<Task> {
        @Override
        public String determineName(Task c) {
            return c.getName();
        }
    }

    /**
     * <p>Returns the sequence of {@link Action} objects which will be executed by this task, in the order of
     * execution.</p>
     *
     * @return The task actions in the order they are executed. Returns an empty list if this task has no actions.
     */

    List<Action<? super Task>> getActions();

    /**
     * <p>Sets the sequence of {@link Action} objects which will be executed by this task.</p>
     *
     * @param actions The actions.
     */
    void setActions(List<Action<? super Task>> actions);

    /**
     * <p>Execute the task only if the given spec is satisfied. The spec will be evaluated at task execution time, not
     * during configuration. If the Spec is not satisfied, the task will be skipped.</p>
     *
     * <p>You may add multiple such predicates. The task is skipped if any of the predicates return false.</p>
     *
     * <p>Typical usage (from Java):</p>
     * <pre>myTask.onlyIf(new Spec&lt;Task&gt;() {
     *    boolean isSatisfiedBy(Task task) {
     *       return isProductionEnvironment();
     *    }
     * });
     * </pre>
     *
     * @param onlyIfSpec specifies if a task should be run
     */
    void onlyIf(Spec<? super Task> onlyIfSpec);

    /**
     * <p>Execute the task only if the given spec is satisfied. The spec will be evaluated at task execution time, not
     * during configuration. If the Spec is not satisfied, the task will be skipped.</p>
     *
     * <p>The given predicate replaces all such predicates for this task.</p>
     *
     * @param onlyIfSpec specifies if a task should be run
     */
    void setOnlyIf(Spec<? super Task> onlyIfSpec);

    /**
     * <p>Adds the given {@link Action} to the beginning of this task's action list.</p>
     *
     * @param action The action to add
     * @return the task object this method is applied to
     */
    Task doFirst(Action<? super Task> action);

    /**
     * <p>Adds the given {@link Action} to the beginning of this task's action list.</p>
     *
     * @param actionName An arbitrary string that is used for logging.
     * @param action The action to add
     * @return the task object this method is applied to
     *
     * @since 4.2
     */
    Task doFirst(String actionName, Action<? super Task> action);

    /**
     * <p>Adds the given {@link Action} to the end of this task's action list.</p>
     *
     * @param action The action to add.
     * @return the task object this method is applied to
     */
    Task doLast(Action<? super Task> action);

    /**
     * <p>Adds the given {@link Action} to the end of this task's action list.</p>
     *
     * @param actionName An arbitrary string that is used for logging.
     * @param action The action to add.
     * @return the task object this method is applied to
     *
     * @since 4.2
     */
    Task doLast(String actionName, Action<? super Task> action);


    /**
     * <p>Returns if this task is enabled or not.</p>
     *
     * @see #setEnabled(boolean)
     */
    boolean getEnabled();

    /**
     * <p>Set the enabled state of a task. If a task is disabled none of the its actions are executed. Note that
     * disabling a task does not prevent the execution of the tasks which this task depends on.</p>
     *
     * @param enabled The enabled state of this task (true or false)
     */
    void setEnabled(boolean enabled);


    /**
     * Returns the execution state of this task. This provides information about the execution of this task, such as
     * whether it has executed, been skipped, has failed, etc.
     *
     * @return The execution state of this task. Never returns null.
     */

    TaskState getState();


    /**
     * <p>Returns the outputs of this task.</p>
     *
     * @return The outputs. Never returns null.
     */
    TaskOutputs getOutputs();
}
