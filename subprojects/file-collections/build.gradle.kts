plugins {
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
dependencies {
    implementation(project(":base-services"))
    implementation(project(":core-api"))
    implementation(project(":files"))
    implementation(project(":model-core"))
    implementation(project(":logging"))
    implementation(project(":native"))



    implementation("com.google.guava:guava:30.1.1-jre")
    implementation("commons-io:commons-io:2.11.0")
    implementation("org.slf4j:slf4j-api:1.7.36")
}