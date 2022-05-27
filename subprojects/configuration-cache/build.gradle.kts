plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation(project(":base-services"))
    implementation(project(":base-services-groovy"))
    implementation(project(":composite-builds"))
    implementation(project(":core"))
    implementation(project(":core-api"))
    implementation(project(":dependency-management"))
    implementation(project(":execution"))
    implementation(project(":file-collections"))
    implementation(project(":file-temp"))
    implementation(project(":file-watching"))
    implementation(project(":functional"))
    implementation(project(":hashing"))
    implementation(project(":launcher"))
    implementation(project(":logging"))
    implementation(project(":messaging"))
    implementation(project(":model-core"))
    implementation(project(":native"))
    implementation(project(":persistent-cache"))
    implementation(project(":plugin-use"))
    implementation(project(":plugins"))
    implementation(project(":process-services"))
/*    implementation(project(":publish"))*/
    implementation(project(":resources"))
    implementation(project(":snapshots"))

    // TODO - move the isolatable serializer to model-core to live with the isolatable infrastructure
    implementation(project(":workers"))

    // TODO - it might be good to allow projects to contribute state to save and restore, rather than have this project know about everything
 /*   implementation(project(":tooling-api"))
    implementation(project(":build-events"))*/
    implementation(project(":native"))
    implementation(project(":build-option"))

    implementation("org.codehaus.groovy:groovy-json:3.0.9") {
        exclude(group = "org.codehaus.groovy", module = "groovy")
    }


    implementation("com.google.guava:guava:30.1.1-jre")

    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.10")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.10")


}