plugins {
    id("java-library")
    id("kotlin")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}


dependencies {

    testImplementation("junit:junit:4.12")
    implementation ("com.esotericsoftware:kryo:5.3.0")
    implementation("javax.inject:javax.inject:1")
    implementation("com.google.guava:guava:30.1.1-jre")
    implementation ("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("commons-lang:commons-lang:2.6")
    implementation("com.googlecode:openbeans:1.0")
    implementation ("org.ow2.asm:asm:9.3")
    implementation ("net.rubygrapefruit:native-platform:0.22-milestone-23")
    implementation("net.rubygrapefruit:file-events:0.22-milestone-23")
    implementation("commons-io:commons-io:2.11.0")
    implementation ("org.slf4j:slf4j-api:1.7.36")
    implementation("org.fusesource.jansi:jansi:2.4.0")
    implementation("it.unimi.dsi:fastutil:8.5.8")
}

tasks.create("TestForGradleTask") {
    doFirst {
        println("First")
    }
    doLast {
        println("Last")
    }
    println(this.actions)
    println(this.state.toString())
    val task2 = tasks.create("task2") {
        println("task2")
    }

    val task1 = tasks.create("task1") {
        println("task1")
    }

    task2.dependsOn(task1)



}

java {
    sourceCompatibility = JavaVersion.VERSION_1_9
    targetCompatibility = JavaVersion.VERSION_1_9
}