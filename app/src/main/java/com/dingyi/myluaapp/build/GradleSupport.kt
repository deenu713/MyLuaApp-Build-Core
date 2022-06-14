package com.dingyi.myluaapp.build

import com.dingyi.terminal.virtual.VirtualExecutable
import com.dingyi.terminal.virtual.VirtualProcessChannel
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.configuration.ConsoleOutput
import org.gradle.api.logging.configuration.ShowStacktrace
import org.gradle.api.logging.configuration.WarningMode
import org.gradle.launcher.TestGradleLauncher
import java.io.File

class GradleSupport(processChannel: VirtualProcessChannel) : VirtualExecutable(processChannel) {
    override fun start(args: Array<out String>): Int {

        val projectPath =
            File(mProcessChannel.cwd)

        System.setProperty("org.gradle.native", "false");
        val launcher = TestGradleLauncher
            .createLauncher {
                it.showStacktrace = ShowStacktrace.INTERNAL_EXCEPTIONS;
                /* setConfigurationCache(BuildOption.Value.value(true));
                 startParameter.setConfigurationCacheDebug(true);*/
                it.warningMode = WarningMode.Fail;
                it.consoleOutput = ConsoleOutput.Rich
                it.logLevel = LogLevel.INFO;
                it.isBuildCacheEnabled = true
                it.isBuildCacheDebugLogging = true
                it.projectDir = projectPath
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
                .redirectOutputStream(mProcessChannel.outputStream)
                .redirectErrorStream(mProcessChannel.errorStream)
                .execute(*args)
            0
        }.getOrElse {
            it.printStackTrace(System.err)
            -1
        }

    }
}