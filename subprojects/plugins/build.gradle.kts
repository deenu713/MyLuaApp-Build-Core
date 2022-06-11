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
    implementation(project(":logging"))
    implementation(project(":process-services"))
    implementation(project(":file-collections"))
    implementation(project(":persistent-cache"))
    implementation(project(":core-api"))
    implementation(project(":model-core"))
    implementation(project(":core"))
    implementation(project(":workers"))
    implementation(project(":dependency-management"))
    implementation(project(":reporting"))
    implementation(project(":platform-base"))
    implementation(project(":platform-jvm"))
    implementation(project(":language-jvm"))
    implementation(project(":language-java"))
    implementation(project(":diagnostics"))
    /*
    implementation(project(":testing-base"))
    implementation(project(":testing-jvm"))*/
    implementation(project(":snapshots"))
    implementation(project(":execution")) {
        because("We need it for BuildOutputCleanupRegistry")
    }

    implementation("org.codehaus.groovy:groovy-templates:3.0.7") {
        exclude(group = "org.codehaus.groovy", module = "groovy")
    }

    implementation("commons-lang:commons-lang:2.6")
    implementation("com.google.guava:guava:30.1.1-jre")



}