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
    implementation(project(":native"))
    implementation(project(":logging"))
    implementation(project(":files"))
    implementation(project(":file-temp"))
    implementation(project(":file-collections"))
    implementation(project(":persistent-cache"))
    implementation(project(":core-api"))
    implementation(project(":model-core"))
    implementation(project(":base-services-groovy"))
    implementation(project(":build-cache"))
    implementation(project(":core"))
    implementation(project(":resources"))
    implementation(project(":resources-http"))
    implementation(project(":snapshots"))
    implementation(project(":execution"))


    implementation("com.google.guava:guava:30.1.1-jre")
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
    implementation("commons-io:commons-io:2.11.0")
    implementation ("org.slf4j:slf4j-api:1.7.36")
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("commons-lang:commons-lang:2.6")
    implementation("javax.inject:javax.inject:1")
    implementation("org.apache.ivy:ivy:2.5.0")

    implementation("org.ow2.asm:asm:9.3")

    implementation("org.ow2.asm:asm-commons:9.3")


    implementation("org.apache.httpcomponents:httpcore:4.4.15")


    implementation("com.google.code.gson:gson:2.8.8")

    implementation("org.apache.maven:maven-settings-builder:3.8.5")


}