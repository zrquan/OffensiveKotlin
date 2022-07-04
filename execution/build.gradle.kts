plugins {
    kotlin("multiplatform")
}

kotlin {
    mingwX64("execution") {
        binaries {
            executable { entryPoint = "main" }
            sharedLib { baseName = "libnative" }
        }
    }

    sourceSets {
        val executionMain by getting
    }
}
