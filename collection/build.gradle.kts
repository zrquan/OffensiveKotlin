plugins {
    kotlin("multiplatform")
}

kotlin {
    mingwX64("collection") {
        binaries {
            executable { entryPoint = "main" }
        }
    }

    sourceSets {
        val collectionMain by getting
    }
}
