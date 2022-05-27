plugins {
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    api(project(":resources"))
    implementation(project(":base-services"))
    implementation(project(":core-api"))
    implementation(project(":core"))
    implementation(project(":model-core"))
    implementation(project(":logging"))


    implementation("commons-lang:commons-lang:2.6")

    implementation("commons-io:commons-io:2.11.0")

    implementation("org.slf4j:slf4j-api:1.7.36")

    implementation("com.google.guava:guava:30.1.1-jre")
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("net.sourceforge.nekohtml:nekohtml:1.9.22")


    implementation("org.apache.httpcomponents:httpclient:4.5.13")



    /*  implementation(libs.jcifs)*/
    implementation("xerces:xercesImpl:2.12.2")

   /* implementation(libs.nekohtml)*/
}