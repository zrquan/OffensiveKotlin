plugins {
    kotlin("multiplatform")
}

kotlin {
    mingwX64("registry") {
//        binaries {
//            executable { entryPoint = "main" }
//        }
    }

    sourceSets {
        val registryMain by getting
    }
}
