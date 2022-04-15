package org.gradle.api.internal.project;

import org.gradle.api.Project;
import org.gradle.api.internal.DomainObjectContext;
import org.gradle.api.internal.GradleInternal;
import org.gradle.api.internal.tasks.TaskContainerInternal;
import org.gradle.internal.service.ServiceRegistry;

public interface ProjectInternal extends Project, DomainObjectContext {

    @Override
    GradleInternal getGradle();

    @Override
    TaskContainerInternal getTasks();


    ServiceRegistry getServices();
}
