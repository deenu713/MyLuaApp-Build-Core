package om.dingyi.myluaapp.build.api

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.configuration.ConsoleOutput
import org.gradle.api.logging.configuration.ShowStacktrace
import org.gradle.api.logging.configuration.WarningMode
import org.gradle.launcher.TestGradleLauncher
import java.io.File
import java.io.PrintStream
import java.util.zip.ZipFile
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        thread {
            extractProjectFromApk()
            runGradle()
        }
    }

    private fun extractProjectFromApk() {
        val apkFile = File(packageCodePath)
        val extractDir = getDefaultProjectDir()
        extractDir.mkdirs()
        val zipFile = ZipFile(apkFile)
        zipFile.entries().asSequence()
            .filter {
                it.name.startsWith("TestProject/")
            }.forEach {
                val file = File(extractDir, it.name)
                println(file)
                file.parentFile?.mkdirs()
                file.createNewFile()
                zipFile.getInputStream(it).use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }

        zipFile.close()
    }

    private fun getDefaultProjectDir(): File {
        return checkNotNull(getExternalFilesDir(null)) {
            "External files dir is null"
        }
    }

    private fun runGradle() {

        val projectPath =
            File(getDefaultProjectDir(), "TestProject")

        System.setProperty("org.gradle.native", "false");
        val launcher = TestGradleLauncher
            .createLauncher {
                it.showStacktrace = ShowStacktrace.ALWAYS_FULL;
                /* setConfigurationCache(BuildOption.Value.value(true));
                 startParameter.setConfigurationCacheDebug(true);*/
                it.warningMode = WarningMode.All;
                it.consoleOutput = ConsoleOutput.Plain
                it.logLevel = LogLevel.INFO;
                it.isBuildCacheEnabled = true
                it.isBuildCacheDebugLogging = true
                it.projectDir = projectPath
                it.gradleUserHomeDir = projectPath.resolve(".gradle_home")
                it.projectCacheDir = projectPath.resolve(".gradle")
                it.isRefreshDependencies = true
            }



        launcher
            /*.apply {
                onCreateGradle { gradle ->
                    println("get gradle")
                    println("gradle: $gradle")
                }
            }*/
            .execute("help")

    }


}