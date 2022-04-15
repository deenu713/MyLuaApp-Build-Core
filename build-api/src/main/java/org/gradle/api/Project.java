package org.gradle.api;

import org.gradle.api.tasks.TaskContainer;

import java.io.File;

public interface Project extends  Comparable<Project> {
    /**
     * <p>Resolves a file path relative to the project directory of this project. This method converts the supplied path
     * based on its type:</p>
     *
     *
     * @param path The object to resolve as a File.
     * @return The resolved file. Never returns null.
     */
    File file(Object path);

    /**
     * <p>Returns the tasks of this project.</p>
     *
     * @return the tasks of this project.
     */
    TaskContainer getTasks();
}
