/*
 * Copyright 2021 the original author or authors.
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

package org.gradle.configurationcache

import org.gradle.api.GradleException
import org.gradle.api.internal.BuildType
import org.gradle.api.internal.StartParameterInternal
import org.gradle.internal.buildtree.BuildActionModelRequirements
import org.gradle.internal.buildtree.BuildModelParameters
import org.gradle.internal.buildtree.BuildTreeModelControllerServices
import org.gradle.internal.buildtree.RunTasksRequirements
import org.gradle.util.internal.IncubationLogger


class DefaultBuildTreeModelControllerServices : BuildTreeModelControllerServices {
    override fun servicesForBuildTree(requirements: BuildActionModelRequirements): BuildTreeModelControllerServices.Supplier {
        val startParameter = requirements.startParameter

        // Isolated projects also implies configuration cache
        if (startParameter.isolatedProjects.get() && !startParameter.configurationCache.get()) {
            if (startParameter.configurationCache.isExplicit) {
                throw GradleException("The configuration cache cannot be disabled when isolated projects is enabled.")
            }
        }

        val isolatedProjects = startParameter.isolatedProjects.get()
        val modelParameters = if (requirements.isCreatesModel) {
            // When creating a model, disable certain features - don't enable configure on demand and only enable configuration cache when isolated projects is enabled
            BuildModelParameters(false, isolatedProjects, isolatedProjects, true)
        } else {
            val configurationCache = startParameter.configurationCache.get() || isolatedProjects
            BuildModelParameters(startParameter.isConfigureOnDemand, configurationCache, isolatedProjects, false)
        }

        if (!startParameter.isConfigurationCacheQuiet) {
            if (modelParameters.isIsolatedProjects) {
                IncubationLogger.incubatingFeatureUsed("Isolated projects")
            } else if (modelParameters.isConfigurationCache) {
                IncubationLogger.incubatingFeatureUsed("Configuration cache")
            }
        }

        return BuildTreeModelControllerServices.Supplier { registration ->
            val buildType = if (requirements.isRunsTasks) BuildType.TASKS else BuildType.MODEL
            registration.add(BuildType::class.java, buildType)
            registration.add(BuildModelParameters::class.java, modelParameters)
            registration.add(BuildActionModelRequirements::class.java, requirements)
        }
    }

    override fun servicesForNestedBuildTree(startParameter: StartParameterInternal): BuildTreeModelControllerServices.Supplier {
        return BuildTreeModelControllerServices.Supplier { registration ->
            registration.add(BuildType::class.java, BuildType.TASKS)
            // Configuration cache is not supported for nested build trees
            registration.add(BuildModelParameters::class.java, BuildModelParameters(startParameter.isConfigureOnDemand, false, false, true))
            registration.add(RunTasksRequirements::class.java, RunTasksRequirements(startParameter))
        }
    }
}
