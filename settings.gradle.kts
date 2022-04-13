pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.aliyun.com/repository/google")
        google()
        mavenCentral()
        maven("https://maven.aliyun.com/repository/public")
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        maven("https://jitpack.io")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven("https://maven.aliyun.com/repository/google")
        google()
        mavenCentral()
        maven("https://maven.aliyun.com/repository/public")
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        maven("https://jitpack.io")
    }
}


include(":app",":build-api")


rootProject.name = "MyLuaApp-Build-Api"

