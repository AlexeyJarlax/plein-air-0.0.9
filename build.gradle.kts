// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.5.2" apply false
    id("com.android.library") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    id("com.google.devtools.ksp") version "2.0.0-1.0.21" apply false
    id("com.google.dagger.hilt.android") version "2.49" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }

//    ext {
//        hilt_version = "2.44" // Убедитесь, что версия актуальна
//    }
//    dependencies {
//        classpath "com.google.dagger:hilt-android-gradle-plugin:$hilt_version"
//    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.0.2")
        classpath("com.google.gms:google-services:4.3.15")
        classpath ("com.google.dagger:hilt-android-gradle-plugin:2.44")
    }
}