package org.gradle.api.invocation;

import org.gradle.api.Project;
import org.gradle.api.execution.TaskExecutionGraph;

/**
 * Represents an invocation of Gradle.
 *
 * <p>You can obtain a {@code Gradle} instance by calling {@link Project#getGradle()}.</p>
 */
public interface Gradle {

    /**
     * Returns the {@link TaskExecutionGraph} for this build.
     *
     * @return The task graph. Never returns null.
     */
    TaskExecutionGraph getTaskGraph();
}
