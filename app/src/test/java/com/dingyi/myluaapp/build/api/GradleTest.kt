package com.dingyi.myluaapp.build.api

import org.gradle.StartParameter
import org.gradle.api.internal.GradleInternal
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.internal.project.ProjectRegistry
import org.gradle.api.services.BuildServiceRegistry
import org.gradle.execution.taskgraph.DefaultTaskExecutionGraph
import org.gradle.execution.taskgraph.TaskExecutionGraphInternal
import org.gradle.internal.service.scopes.BuildScopeServiceRegistryFactory
import org.gradle.internal.service.scopes.GradleScopeServices
import org.gradle.internal.service.scopes.ServiceRegistryFactory
import org.gradle.invocation.DefaultGradle
import org.junit.Test


class TestGradle(
    parent: GradleInternal,
    startParameter:StartParameter ,
    parentRegistry: ServiceRegistryFactory
) : DefaultGradle(parent, startParameter, parentRegistry) {


    override fun getTaskGraph(): TaskExecutionGraphInternal {
        TODO("Not yet implemented")
    }

    override fun getSharedServices(): BuildServiceRegistry {
        TODO("Not yet implemented")
    }

    override fun getProjectRegistry(): ProjectRegistry<ProjectInternal> {
        TODO("Not yet implemented")
    }

}

class GradleTest {


    @Test
    fun test1() {

        val defaultGradle = TestGradle(
            null,
            StartParameter(),
            BuildScopeServiceRegistryFactory(GradleScopeServices())
        )
    }
}