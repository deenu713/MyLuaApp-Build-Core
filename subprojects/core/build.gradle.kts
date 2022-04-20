plugins {
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}


dependencies {

    implementation(project(":base-services"))
    implementation(project(":functional"))
    implementation(project(":messaging"))
    implementation(project(":logging"))
    implementation(project(":resources"))
    implementation(project(":cli"))
    implementation(project(":build-option"))
    implementation(project(":native"))
    implementation(project(":model-core"))
    implementation(project(":persistent-cache"))
    implementation(project(":build-cache"))
    implementation(project(":build-cache-packaging"))
    implementation(project(":core-api"))
    implementation(project(":files"))
    implementation(project(":file-temp"))
    implementation(project(":file-collections"))
    implementation(project(":process-services"))
    //implementation(project(":jvm-services"))
    implementation(project(":snapshots"))
    implementation(project(":file-watching"))
    implementation(project(":execution"))
    implementation(project(":normalization-java"))


    implementation ("org.ow2.asm:asm:9.3")
    implementation ("commons-lang:commons-lang:2.6")
    implementation("com.google.guava:guava:30.1.1-jre")
    implementation("commons-io:commons-io:2.11.0")
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation ("net.rubygrapefruit:native-platform:0.22-milestone-23")
    implementation("org.ow2.asm:asm-commons:9.3")


}