plugins {
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation(project(":base-services"))
    implementation(project(":base-services-groovy"))
    implementation(project(":functional"))
    implementation(project(":logging"))
    implementation(project(":file-collections"))
    implementation(project(":core-api"))
    implementation(project(":model-core"))
    implementation(project(":core"))
    implementation(project(":reporting"))
    implementation(project(":platform-base"))
    implementation(project(":snapshots"))
    implementation(project(":dependency-management"))
    implementation(project(":base-services-groovy"))
    implementation(project(":build-option"))

    implementation("org.codehaus.groovy:groovy-json:3.0.7") {
        exclude(group = "org.codehaus.groovy", module = "groovy")
    }


    implementation("commons-lang:commons-lang:2.6")
    implementation("com.google.guava:guava:30.1.1-jre")

    implementation ("com.googlecode.jatl:jatl:0.2.3")
   
}