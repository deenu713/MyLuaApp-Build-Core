package com.dingyi.myluaapp.build.api



import org.gradle.launcher.TestGradleLauncher
import org.junit.Test
import java.io.File


class GradleTest {


    @Test
    fun test1() {
        val launcher = TestGradleLauncher
            .createLauncher {
                it.projectDir = File("G:\\android studio project\\AideLua")
            }
    }
}