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
    implementation(project(":messaging"))
    implementation(project(":logging"))
    implementation(project(":resources"))
    implementation(project(":cli"))
    implementation(project(":build-option"))
    implementation(project(":native"))
    implementation(project(":model-core"))
    implementation(project(":persistent-cache"))
    implementation(project(":build-cache"))
    implementation(project(":build-cache-packaging"))
    implementation(project(":core-api"))
    implementation(project(":files"))
    implementation(project(":file-temp"))
    implementation(project(":file-collections"))
    implementation(project(":process-services"))
    implementation(project(":jvm-services"))
    implementation(project(":model-groovy"))
    implementation(project(":snapshots"))
    implementation(project(":file-watching"))
    implementation(project(":execution"))
    //implementation(project(":worker-processes"))
    implementation(project(":normalization-java"))




    implementation("org.codehaus.groovy:groovy-astbuilder:3.0.7") {
        exclude(group = "org.codehaus.groovy", module = "groovy")
    }
    implementation("org.codehaus.groovy:groovy-templates:3.0.7") {
        exclude(group = "org.codehaus.groovy", module = "groovy")
    }
    implementation("org.codehaus.groovy:groovy-xml:3.0.7") {
        exclude(group = "org.codehaus.groovy", module = "groovy")
    }
    /* implementation("org.codehaus.groovy:groovy-console:3.0.7") {
         exclude(group = "org.codehaus.groovy", module = "groovy")
     }

     implementation("org.codehaus.groovy:groovy-json:3.0.7") {
         exclude(group = "org.codehaus.groovy", module = "groovy")
     }

     implementation("org.codehaus.groovy:groovy-dateutil:3.0.7") {
         exclude(group = "org.codehaus.groovy", module = "groovy")
     }
     implementation("org.codehaus.groovy:groovy-datetime:3.0.7") {
         exclude(group = "org.codehaus.groovy", module = "groovy")
     }
     implementation("org.codehaus.groovy:groovy-groovydoc:3.0.7") {
         exclude(group = "org.codehaus.groovy", module = "groovy")
     }
     */
    implementation("org.codehaus.groovy:groovy-json:3.0.7") {
        exclude(group = "org.codehaus.groovy", module = "groovy")
    }
    /* implementation("org.codehaus.groovy:groovy-nio:3.0.7")
     {
         exclude(group = "org.codehaus.groovy", module = "groovy")
     }
     implementation("org.codehaus.groovy:groovy-sql:3.0.7")
     {
         exclude(group = "org.codehaus.groovy", module = "groovy")
     }
     implementation("org.codehaus.groovy:groovy-test:3.0.7")
     {
         exclude(group = "org.codehaus.groovy", module = "groovy")
     }*/


    implementation("org.ow2.asm:asm:9.3")
    implementation("commons-lang:commons-lang:2.6")
    implementation("com.google.guava:guava:30.1.1-jre")
    implementation("commons-io:commons-io:2.11.0")
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("net.rubygrapefruit:native-platform:0.22-milestone-23")
    implementation("org.ow2.asm:asm-commons:9.3")


    implementation("org.apache.commons:commons-compress:1.21")



}