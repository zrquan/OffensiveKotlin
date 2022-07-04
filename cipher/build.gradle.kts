plugins {
    kotlin("multiplatform")
}

val mingw64Path = File("C:/Users/4shen0ne/scoop/apps/msys2/current/mingw64")

kotlin {
    mingwX64("cipher") {
        compilations["main"].apply {
            val libsodium by cinterops.creating {
                defFile = project.file("src/cinterop/libsodium.def")
                includeDirs.headerFilterOnly(mingw64Path.resolve("include"))
            }
            // include the static lib
            kotlinOptions.freeCompilerArgs += listOf("-include-binary", "$mingw64Path/lib/libsodium.a")
        }
        binaries {
            executable { entryPoint = "main" }
        }
    }

    sourceSets {
        val cipherMain by getting
    }
}
