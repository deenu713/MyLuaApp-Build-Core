plugins {
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation(project(":base-services"))
    implementation(project(":logging"))
    implementation(project(":messaging"))
    implementation(project(":native"))
    implementation(project(":process-services"))

}