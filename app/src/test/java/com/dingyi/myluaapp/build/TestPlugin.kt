package com.dingyi.myluaapp.build

import groovy.lang.Closure
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.TaskAction

class TestPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.logger.info("test plugin")
        target.task("sync") { task ->

            target
                .configurations
                .forEach {
                    target.logger.info("configuration: ${it.name}")

                }

            /*target
                .configurations
                .matching {
                    it.isCanBeResolved
                }
                .forEach { configuration ->
                    configuration.dependencies.forEach {
                        task.inputs
                            .property("${it.group}:${it.name}:${it.version}", it.toString())


                        task.outputs
                            .file(it)

                    }
                }

            task.doLast {
                target.logger.info("start sync task")
                target.configurations
                    .matching {
                        it.isCanBeResolved
                    }
                    .forEach { configuration ->
                        //resolve dependencies
                        configuration.resolve()

                    }
            }
*/
        }
            //config input


    }
}

