plugins {
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation(project(":base-services"))

    api("io.github.dingyi222666:groovy-android:1.0.4-20220611.222542-10")
    implementation("com.android.tools:r8:3.3.28")
    api("org.codehaus.groovy:groovy:3.0.7:grooid")
    implementation("com.google.guava:guava:30.1.1-jre")
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")

}