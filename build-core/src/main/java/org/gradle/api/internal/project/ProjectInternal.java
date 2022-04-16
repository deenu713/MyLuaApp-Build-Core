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

package org.gradle.api.internal.project;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.internal.DomainObjectContext;
import org.gradle.api.internal.GradleInternal;
import org.gradle.api.internal.file.HasScriptServices;
import org.gradle.api.provider.Property;
import org.gradle.internal.scan.UsedByScanPlugin;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.util.Path;

import javax.annotation.Nullable;
import java.util.Set;

@UsedByScanPlugin("scan, test-retry")
public interface ProjectInternal extends Project, ProjectIdentifier, HasScriptServices, DomainObjectContext /*, ModelRegistryScope, PluginAwareInternal */ {

    // These constants are defined here and not with the rest of their kind in HelpTasksPlugin because they are referenced
    // in the ‘core’ modules, which don't depend on ‘plugins’ where HelpTasksPlugin is defined.
    String HELP_TASK = "help";
    String TASKS_TASK = "tasks";
    String PROJECTS_TASK = "projects";


    @Override
    @UsedByScanPlugin("test-distribution, test-retry")
    GradleInternal getGradle();

    /**
     * Returns a unique path for this project within the current build tree.
     */
    Path getIdentityPath();




    /**
     * Returns the property that stored .
     * <p>
     * By exposing this property, the {@code base} plugin can override the default value without overriding the build configuration.
     * <p>
     * See: https://github.com/gradle/gradle/issues/16946
     */
    Property<Object> getInternalStatus();

}
