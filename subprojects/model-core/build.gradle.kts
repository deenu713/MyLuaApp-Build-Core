plugins {
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    api(project(":core-api"))
    api(project(":problems"))

    implementation(project(":base-services"))
    implementation(project(":base-services-groovy"))
    implementation(project(":functional"))
    implementation(project(":logging"))
    implementation(project(":messaging"))
    implementation(project(":persistent-cache"))
    implementation(project(":snapshots"))

    //implementation(libs.futureKotlin("stdlib"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.10")
    implementation("com.google.guava:guava:30.1.1-jre")
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation ("org.ow2.asm:asm:9.3")

    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("commons-lang:commons-lang:2.6")
}