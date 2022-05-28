package com.dingyi.myluaapp.build

import org.gradle.api.Plugin
import org.gradle.api.Project

class TestPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.logger.info("test plugin")
        target.task("testTask") {
            it.doLast {
                target.logger.info("test task")
            }
        }
    }
}