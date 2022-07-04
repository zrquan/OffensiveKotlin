plugins {
    kotlin("multiplatform")
}

val javaHome = File(System.getenv("JAVA_HOME"))
val includeBase = javaHome.resolve("include")
val includeWin32 = includeBase.resolve("win32")

val mingwPath = File("C:\\Users\\4shen0ne\\scoop\\apps\\msys2\\current\\mingw64")

kotlin {
    mingwX64("jvmti") {
        compilations["main"].cinterops {
            val jvmti by creating {
                defFile = project.file("src/cinterop/jvmti.def")
                includeDirs.allHeaders(mingwPath, includeBase, includeWin32)
            }
        }
        binaries {
            sharedLib {
                baseName = "libnative"
            }
        }
    }

    sourceSets {
        val jvmtiMain by getting
    }
}
