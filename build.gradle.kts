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

            // 🚀 HIGH-PERFORMANCE NATIVE IMAGE OPTIMIZATIONS (by AI)
            buildArgs.addAll(listOf(
                "-O3",
                "--gc=epsilon",
                "-H:+InlineEverything",
                "-H:+UnlockExperimentalVMOptions",
                "-H:Optimize=2",
                "--enable-preview"
            ))
        }

        // Optional: Add PGO
        create("pgo") {
            imageName.set("day12-pgo")
            fallback.set(false)
            buildArgs.addAll(listOf(
                "-O3",
                "--gc=g1",
                "-H:InlineEverything=true",
                "-H:Optimize=2",
                "--enable-preview",
                "--pgo-instrument"
            ))
        }
    }
}
