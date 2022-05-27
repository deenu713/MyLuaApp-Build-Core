package com.dingyi.myluaapp.build


import org.gradle.internal.build.BuildStateRegistry
import org.gradle.internal.buildtree.BuildTreeModelControllerServices
import org.gradle.internal.service.scopes.GlobalScopeServices
import org.gradle.launcher.TestGradleLauncher
import org.junit.Test
import org.junit.runner.RunWith
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.JarURLConnection
import java.net.URL


class GradleTest {


    private fun extractImplementationClassNames(resource: URL): List<String>? {

        val urlConnection = resource.openConnection()

        val inputStream = urlConnection.getInputStream()
        return kotlin.runCatching {
            val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            val implementationClassNames = mutableListOf<String>()
            reader.forEachLine {
                val line = it.replace("#.*".toRegex(), "").trim { it <= ' ' }
                if (line.isNotEmpty()) {
                    implementationClassNames.add(line)
                }

            }
            implementationClassNames
        }.onFailure {
            inputStream.close()
        }.getOrNull()
    }


    private fun copyTestResourcesToLocalResources() {
        val path = File("").canonicalFile.resolve("src/test/resources")
        val localResourcePath = this.javaClass.classLoader
            .getResource("META-INF")


        path.copyRecursively(File(localResourcePath.toURI()),true)
    }

    @Test
    fun test1() {
        copyTestResourcesToLocalResources()
        val projectPath = File("G:\\android studio project\\AideLua")
        val launcher = TestGradleLauncher
            .createLauncher {
                it.projectDir = projectPath
            }


        val resource =
            this.javaClass.classLoader.getResource("META-INF/services/org.gradle.internal.service.scopes.PluginServiceRegistry")

        println(extractImplementationClassNames(resource))


        launcher
            .create()
            .whenComplete { gradle, throwable ->
                println(gradle)
            }
    }
}