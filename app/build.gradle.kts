plugins {
    id("com.android.application")
    id ("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.android")
    kotlin("plugin.serialization") version "2.0.0"
    id("kotlin-kapt") // плагин kotlin-kapt для работы зависимостей kapt("
    id("com.google.devtools.ksp")
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
        versionCode = 8
        versionName = "0.0.8"

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
        encoding = "UTF-8"
    }
    kotlinOptions {
        jvmTarget = "17" // более новая сборка виртуальной машины: 17
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

    packagingOptions {
        resources {
            excludes += "META-INF/DEPENDENCIES"
        }
    }

    hilt {
        enableAggregatingTask = true
    }

    kapt {
        correctErrorTypes = true
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
    implementation("com.google.dagger:hilt-android:2.52")
    implementation("androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha03")
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    kapt("com.google.dagger:hilt-compiler:2.52")
    ksp("com.google.dagger:hilt-compiler:2.52")
    implementation (libs.androidx.hilt.lifecycle.viewmodel)

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

    // glide для пикч
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

// ТЕСТИРОВАНИЕ

    // Фреймворк для написания и запуска тестов в Java.
    testImplementation(libs.junit)

    // Библиотека для написания и запуска тестов на Android.
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

// ПРОДВИНУТЫЕ

    // Библиотека для управления зависимостями koin и Dagger Hilt
    implementation (libs.hilt.android)
    ksp (libs.google.hilt.compiler)
    implementation(libs.androidx.hilt.lifecycle.viewmodel)
    ksp(libs.androidx.hilt.compiler)

    // Расширения Kotlin для работы с жизненным циклом компонентов.
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Расширения Kotlin для фрагментов
    implementation(libs.androidx.fragment.ktx)

    // Компонент ViewPager2 для реализации горизонтальных и вертикальных листалок.
    implementation(libs.androidx.viewpager2)

    // Jetpack Navigation Component
//    implementation(libs.androidx.navigation.fragment.ktx)
//    implementation(libs.androidx.navigation.ui.ktx)
//    implementation(libs.androidx.fragment.ktx.v181)
//    implementation(libs.androidx.hilt.navigation.compose)

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

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.play.services.auth)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.firestore)
    implementation(libs.com.google.firebase.firebase.analytics2)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.appcheck.playintegrity)
    implementation (libs.firebase.database)
    implementation (libs.firebase.core)

    // Jetpack Compose
    implementation(libs.androidx.material)

    //изображения
    implementation(libs.picasso)

    //карты
    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)
    implementation(libs.flogger)
    implementation(libs.flogger.system.backend) // System backend
    implementation(libs.flogger.log4j2.backend) // Log4j2 backend (опционально)

}