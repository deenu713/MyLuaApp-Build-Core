plugins {
    id("java-library")
}



java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {

    implementation(project(":base-services"))
    implementation(project(":messaging"))
    implementation(project(":cli"))
    implementation(project(":build-option"))

    implementation(project(":native"))

    api("org.slf4j:slf4j-api:1.7.36")

    implementation("org.fusesource.jansi:jansi:2.4.0")
    implementation ("commons-lang:commons-lang:2.6")
    implementation("it.unimi.dsi:fastutil:8.5.8")
    implementation("com.google.guava:guava:30.1.1-jre")
    implementation ("com.esotericsoftware:kryo:5.3.0")

}
