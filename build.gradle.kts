// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.4" apply false // Плагин для Android приложений.
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false // Плагин для Kotlin в Android.
}

buildscript {
    dependencies {
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.46.1") // Hilt Gradle плагин
    }
}