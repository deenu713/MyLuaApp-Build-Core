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
    implementation(project(":process-services"))
    implementation(project(":worker-processes"))
    implementation(project(":file-collections"))
    implementation(project(":file-temp"))
    implementation(project(":core-api"))
    implementation(project(":model-core"))
    implementation(project(":core"))
    implementation(project(":jvm-services"))
    implementation(project(":workers"))
    implementation(project(":platform-base"))
    implementation(project(":platform-jvm"))
    implementation(project(":language-jvm"))
    implementation(project(":language-java"))
    implementation(project(":files"))

    //TODO create android-platform directory to put compiler plugin
   /* implementation("io.github.dingyi222666:groovy-android:1.0.2")*/
    implementation("org.codehaus.groovy:groovy:3.0.9:grooid")

    implementation("org.ow2.asm:asm:9.3")

    implementation("com.google.guava:guava:30.1.1-jre")

    implementation("org.codehaus.groovy:groovy-groovydoc:3.0.9") {
        exclude(group = "org.codehaus.groovy", module = "groovy")
    }


}