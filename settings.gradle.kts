pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.aliyun.com/repository/google")

        maven("https://maven.aliyun.com/repository/public")
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://repo.gradle.org/gradle/libs-releases")

    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven("https://maven.aliyun.com/repository/google")

        maven("https://maven.aliyun.com/repository/public")
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://repo.gradle.org/gradle/libs-releases")
        maven("https://maven.repository.redhat.com/ga/")
    }
}


include(":app")


rootProject.name = "MyLuaApp-Build-Api"

include(":core-api")
include(":base-annotations")
include(":hashing")
include(":build-operations")
include(":base-services")
include(":files")
include(":messaging")
include(":cli")
include(":build-option")
include(":logging")
include(":native")
include(":file-temp")
include(":process-services")
include(":resources")
include(":persistent-cache")
