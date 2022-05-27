plugins {
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation(project(":base-services"))
    implementation(project(":base-services-groovy"))
    implementation(project(":messaging"))
    implementation(project(":logging"))
    implementation(project(":process-services"))
    implementation(project(":worker-processes"))
    implementation(project(":files"))
    implementation(project(":file-collections"))
    implementation(project(":file-temp"))
    implementation(project(":persistent-cache"))
    implementation(project(":jvm-services"))
    implementation(project(":core-api"))
    implementation(project(":model-core"))
    implementation(project(":core"))
    implementation(project(":workers"))
    implementation(project(":snapshots"))
    implementation(project(":execution"))
    implementation(project(":dependency-management"))
    implementation(project(":platform-base"))
    implementation(project(":platform-jvm"))
    implementation(project(":language-jvm"))
   /* implementation(project(":build-events"))
    implementation(project(":tooling-api"))*/
    

    implementation("org.ow2.asm:asm:9.3")
    implementation("commons-lang:commons-lang:2.6")
    implementation("com.google.guava:guava:30.1.1-jre")
    implementation("commons-io:commons-io:2.11.0")
    implementation("org.slf4j:slf4j-api:1.7.36")

    implementation("org.ow2.asm:asm-commons:9.3")

    implementation("org.apache.commons:commons-compress:1.21")
    implementation("it.unimi.dsi:fastutil:8.5.8")

}