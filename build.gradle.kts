plugins {
    id("java")
}

group = "com.panagiotispetridis"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("com.google.ortools:ortools-java:9.10.4067")
}

tasks.test {
    useJUnitPlatform()
}