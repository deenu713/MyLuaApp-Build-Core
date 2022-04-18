plugins {
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    api(project(":files"))
    api(project(":hashing"))

    implementation(project(":base-annotations"))

    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("com.google.guava:guava:30.1.1-jre")
}