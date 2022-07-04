plugins {
    kotlin("multiplatform")
}

kotlin {
    mingwX64("discovery") {
        binaries {
            executable { entryPoint = "main" }
        }
    }

    sourceSets {
        val discoveryMain by getting {
            dependencies {
                api(project(":registry"))
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
            }
        }
    }
}
