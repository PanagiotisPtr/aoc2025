plugins {
    id("java")
    id("application")
    id("org.graalvm.buildtools.native") version "0.10.2"
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

application {
    mainClass.set("com.panagiotispetridis.day12.Main")
}

graalvmNative {
    toolchainDetection = false
    binaries {
        named("main") {
            imageName.set("day12")
            fallback.set(false)
            buildArgs.add("-O3")
            buildArgs.add("--enable-preview")
        }
    }
}

tasks.withType<JavaExec> {
    jvmArgs = listOf(
        "-XX:+UseZGC",
        "-Xms8g",
        "-Xmx8g"
    )
}