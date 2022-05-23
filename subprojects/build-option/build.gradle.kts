plugins {
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation(project(":cli"))

    implementation(project(":base-annotations"))
    implementation("commons-lang:commons-lang:2.6")
}