plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {

    compileSdk = 32
    buildToolsVersion = "32.0.0"

    defaultConfig {
        applicationId = "com.dingyi.myluaapp.build.api"
        minSdk = 26
        targetSdk = 32

        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
    }

    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/kotlin")
           /* resources.srcDirs("src/main/res")*/

        }
        getByName("test") {
            java.srcDirs("src/test/kotlin")
            resources.srcDirs("src/test/res")
        }
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            isMinifyEnabled = false

        }
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    packagingOptions {
        resources.excludes.addAll(listOf("META-INF/**", "xsd/*", "license/*"))
        resources.pickFirsts.add("kotlin/**")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}


dependencies {
    //implementation(project(":build-core"))
    implementation ("androidx.core:core-ktx:1.7.0")
    implementation ("androidx.appcompat:appcompat:1.4.1")
    implementation ("com.google.android.material:material:1.4.0")
    testImplementation ("junit:junit:4.13.2")

    androidTestImplementation ("androidx.test.ext:junit:1.1.3")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.4.0")

    implementation(project(":model-groovy"))
    implementation(project(":file-collections"))
    implementation(project(":file-watching"))
    implementation(project(":file-temp"))
    implementation(project(":build-cache-packaging"))

/*
    implementation(project(":launcher"))
    implementation(project(":core"))
    implementation(project(":logging"))
    implementation(project(":core-api"))
    implementation(project(":base-services"))
    implementation(project(":configuration-cache"))
*/



//    implementation ("com.esotericsoftware:kryo:5.3.0")
//    implementation("javax.inject:javax.inject:1")
//    implementation("com.google.guava:guava:30.1.1-jre")
//    implementation ("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
//    implementation("com.google.code.findbugs:jsr305:3.0.2")
//    implementation("commons-lang:commons-lang:2.6")
//    implementation("com.googlecode:openbeans:1.0")
//    implementation ("org.ow2.asm:asm:9.3")
//    implementation ("net.rubygrapefruit:native-platform:0.22-milestone-23")
//    implementation("net.rubygrapefruit:file-events:0.22-milestone-23")
//    implementation("commons-io:commons-io:2.11.0")
//    implementation("org.apache.commons:commons-compress:1.21")
//    implementation ("org.slf4j:slf4j-api:1.7.36")
//    implementation("org.fusesource.jansi:jansi:2.4.0")
//    implementation("it.unimi.dsi:fastutil:8.5.8")

}