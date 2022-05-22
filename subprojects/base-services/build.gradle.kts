plugins {
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    api(project(":base-annotations"))
    api(project(":hashing"))
    api(project(":build-operations"))

    implementation("com.google.guava:guava:30.1.1-jre")
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
    implementation("commons-io:commons-io:2.11.0")
    implementation ("org.slf4j:slf4j-api:1.7.36")
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("commons-lang:commons-lang:2.6")
    implementation("javax.inject:javax.inject:1")

    implementation("org.ow2.asm:asm:9.3")

    implementation("commons-io:commons-io:2.11.0")

}