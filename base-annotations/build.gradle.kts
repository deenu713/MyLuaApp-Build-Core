plugins {
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_7
    targetCompatibility = JavaVersion.VERSION_1_7
}

dependencies {
    api("javax.inject:javax.inject:1")
    api("com.google.code.findbugs:jsr305:3.0.2")
}