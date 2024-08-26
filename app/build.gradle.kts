plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("plugin.serialization") version "2.0.0"
    id("kotlin-kapt") // плагин kotlin-kapt для работы зависимостей kapt("
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin") // Плагин Hilt для внедрения зависимостей.
    id("com.google.gms.google-services")
}

android {
    namespace = "com.pavlovalexey.pleinair"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.pavlovalexey.pleinair"
        resourceConfigurations += setOf("ru", "en")
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            isMinifyEnabled = false
            // applicationIdSuffix = ".debug" не делаю суфикс, чтобы файл нашелся файрбазой
            // Включаем отладку
//            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17 // более новая сборка виртуальной машины: 17
        targetCompatibility = JavaVersion.VERSION_17 // более новая сборка виртуальной машины: 17
    }
    kotlinOptions {
        jvmTarget = "17" // более новая сборка виртуальной машины: 17
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
// БАЗОВЫЕ

    // Расширения Kotlin для работы с Activity.
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.ktx)

    // Библиотека для работы с API Android.
    implementation(libs.androidx.core.ktx)

    // Библиотека для поддержки современного дизайна пользовательского интерфейса.
    implementation(libs.material)

    // Библиотека для обеспечения совместимости с новыми возможностями платформы Android на более старых устройствах.
    implementation(libs.androidx.appcompat)

    // Библиотека для создания сложных макетов пользовательского интерфейса.
    implementation(libs.androidx.constraintlayout)

    // Библиотеки для работы с изображениями и их кэширования.
    implementation(libs.glide)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    ksp(libs.compiler)

//ДАННЫЕ

    // Библиотека для сериализации и десериализации JSON.
    implementation(libs.gson)

    // Библиотека для работы с JSON на Kotlin.
    implementation(libs.kotlinx.serialization.json)

    // HTTP-клиент для обмена данными с удаленными серверами.
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)

    // Библиотека для создания функций отправки в Intents с помощью mailto: URI
    implementation(libs.email.intent.builder)

// ТЕСТИРОВАНИЕ

    // Фреймворк для написания и запуска тестов в Java.
    testImplementation(libs.junit)

    // Библиотека для написания и запуска тестов на Android.
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

// ПРОДВИНУТЫЕ

    // Библиотека для управления зависимостями.
    implementation(libs.koin.android)

    // Расширения Kotlin для работы с жизненным циклом компонентов.
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx.v283)

    // Расширения Kotlin для фрагментов
    implementation(libs.androidx.fragment.ktx)

    // Компонент ViewPager2 для реализации горизонтальных и вертикальных листалок.
    implementation(libs.androidx.viewpager2)

    // Jetpack Navigation Component
//    implementation(libs.androidx.navigation.fragment.ktx)
//    implementation(libs.androidx.navigation.ui.ktx)
//    implementation(libs.androidx.fragment.ktx.v181)

    // корутин
    implementation(libs.kotlinx.coroutines.android.v180)

    // Permission для работы с корутин
    implementation(libs.peko)

    // библиотека Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // поддержка легаси для ярлычков (надо или нет я хз)
    implementation(libs.androidx.legacy.support.v4)

    // Hilt для внедрения зависимостей
    implementation(libs.hilt.android)
    kapt(libs.google.hilt.compiler)
    implementation(libs.hilt.android.v244)
    kapt(libs.dagger.hilt.android.compiler)
    kapt(libs.dagger.compiler)

    // Jetpack Compose Hilt Integration
//    implementation(libs.androidx.hilt.navigation.compose)

    // Firebase Authentication
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.play.services.auth)
    implementation(libs.firebase.storage.ktx)
//    implementation 'com.google.firebase:firebase-auth-ktx'
//    implementation 'com.google.firebase:firebase-firestore-ktx'
//    implementation 'com.google.firebase:firebase-storage-ktx'
//    implementation 'com.google.firebase:firebase-crashlytics-ktx'
//    implementation 'com.google.firebase:firebase-analytics-ktx'
//    implementation 'com.google.firebase:firebase-messaging-ktx'

//    implementation(libs.firebase.auth.v2200)

//    implementation platform("com.google.firebase:firebase-bom:32.0.0")

    // Jetpack Compose
    implementation(libs.androidx.material)
}