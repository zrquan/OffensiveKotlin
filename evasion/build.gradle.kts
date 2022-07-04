plugins {
    kotlin("multiplatform")
}

kotlin {
    mingwX64("evasion") {
        binaries {
            executable { entryPoint = "main" }
        }
    }

    sourceSets {
        val evasionMain by getting
    }
}
