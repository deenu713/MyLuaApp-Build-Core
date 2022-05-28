package com.dingyi.myluaapp.build

import org.gradle.api.Plugin
import org.gradle.api.Project

class TestPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.logger.info("test plugin")
        target.task("sync") { task ->


            task.doFirst {

                target.configurations
                    .matching { it.isCanBeResolved }
                    .configureEach {
                        it.resolve()
                    }
                target.logger.info("test task")
            }
        }
    }
}