plugins {
    kotlin("multiplatform")
}

kotlin {
    mingwX86("shellcode") {
        binaries {
            executable { entryPoint = "main" }
        }
    }

    sourceSets {
        val shellcodeMain by getting
    }
}
