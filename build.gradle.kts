buildscript {
    repositories {
        mavenCentral()
    }

    val kotlinVersion: String by rootProject
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

allprojects {
    repositories {
        mavenCentral()
    }
}
