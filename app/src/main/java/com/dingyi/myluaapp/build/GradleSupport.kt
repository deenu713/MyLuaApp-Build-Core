package com.dingyi.myluaapp.build

import com.dingyi.terminal.virtualprocess.VirtualExecutable
import com.dingyi.terminal.virtualprocess.VirtualProcessEnvironment
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.configuration.ConsoleOutput
import org.gradle.api.logging.configuration.ShowStacktrace
import org.gradle.api.logging.configuration.WarningMode
import org.gradle.launcher.TestGradleLauncher
import java.io.File

class GradleSupport(processChannel: VirtualProcessEnvironment) : VirtualExecutable(processChannel) {
    override fun start(args: Array<out String>): Int {

        val projectPath =
            File(mProcessEnvironment.currentWorkDir)

        System.setProperty("org.gradle.native", "true");
        val launcher = TestGradleLauncher
            .createLauncher {
                it.showStacktrace = ShowStacktrace.ALWAYS_FULL;
                /* setConfigurationCache(BuildOption.Value.value(true));
                 startParameter.setConfigurationCacheDebug(true);*/
                it.warningMode = WarningMode.Fail;
                it.consoleOutput = ConsoleOutput.Rich
                it.logLevel = LogLevel.LIFECYCLE;
                it.isBuildCacheEnabled = true
                it.isBuildCacheDebugLogging = true
                it.projectDir = projectPath
                it.currentDir = File(mProcessEnvironment.currentWorkDir)
                it.gradleUserHomeDir = projectPath.resolve(".gradle_home")
                it.projectCacheDir = projectPath.resolve(".gradle")
                it.isRefreshDependencies = true
            }




        return runCatching {
            launcher
                /*.apply {
                onCreateGradle { gradle ->
                    println("get gradle")
                    println("gradle: $gradle")
                }
            }*/
                .redirectOutputStream(mProcessEnvironment.outputStream)
                .redirectErrorStream(mProcessEnvironment.errorStream)
                .execute(*args)
            0
        }.getOrElse {
            it.printStackTrace(System.err)
            -1
        }

    }
}