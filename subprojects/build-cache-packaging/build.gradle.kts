plugins {
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    api(project(":build-cache-base"))
    api(project(":snapshots"))
    api(project(":hashing"))
    api(project(":files"))

    implementation(project(":base-annotations"))

    implementation("commons-io:commons-io:2.11.0")
    implementation("com.google.guava:guava:30.1.1-jre")
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")

    implementation("org.apache.commons:commons-compress:1.21")

}