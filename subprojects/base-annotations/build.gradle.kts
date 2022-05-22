plugins {
    id("java-library")
}


dependencies {
    api("javax.inject:javax.inject:1")
    api("com.google.code.findbugs:jsr305:3.0.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}