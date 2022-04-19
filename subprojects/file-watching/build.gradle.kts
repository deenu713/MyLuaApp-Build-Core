plugins {
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {

    api(project(":snapshots"))

    implementation(project(":base-annotations"))
    implementation(project(":build-operations"))

    implementation("com.google.guava:guava:30.1.1-jre")
    implementation("org.slf4j:slf4j-api:1.7.36")

    implementation ("net.rubygrapefruit:native-platform:0.22-milestone-23")
    implementation("net.rubygrapefruit:file-events:0.22-milestone-23")
}