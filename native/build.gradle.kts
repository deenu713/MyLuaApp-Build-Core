plugins {
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    api(project(":files"))
    implementation(project(":base-services"))
    implementation(project(":file-temp"))

    implementation ("net.rubygrapefruit:native-platform:0.22-milestone-23")
    implementation("net.rubygrapefruit:file-events:0.22-milestone-23")
    implementation("org.fusesource.jansi:jansi:2.4.0")

    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("com.google.guava:guava:30.1.1-jre")
    implementation("commons-io:commons-io:2.11.0")


}