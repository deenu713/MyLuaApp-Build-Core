import com.android.build.gradle.internal.tasks.factory.dependsOn

plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    compileSdk = 31

    defaultConfig {
        minSdk = 26
        targetSdk = 32

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {

    testCompileOnly("org.gradle:gradle-api:7.2.0")
    implementation("javax.inject:javax.inject:1")
    implementation("com.google.guava:guava:24.0-android")
    implementation ("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("commons-lang:commons-lang:2.6")

}

tasks.create("TestForGradleTask") {
    doFirst {
        println("First")
    }
    doLast {
        println("Last")
    }
    println(this.actions)
    println(this.state.toString())
    val task2 = tasks.create("task2") {
        println("task2")
    }

    val task1 = tasks.create("task1") {
        println("task1")
    }

    task2.dependsOn(task1)



}
