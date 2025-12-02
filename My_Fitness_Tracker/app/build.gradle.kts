plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.fitness_kzo"
    compileSdk = 35 // Use the latest stable SDK version

    defaultConfig {
        applicationId = "com.example.fitness_kzo"
        minSdk = 24 // Minimum SDK version your app supports
        targetSdk = 35 // Target SDK version for your app
        versionCode = 1 // Increment this for each release
        versionName = "1.0" // Version name for the user

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false // Set to true for release builds to reduce APK size
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro" // Custom ProGuard rules
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11 // Java version compatibility
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11" // Kotlin JVM target version
    }

    buildFeatures {
        viewBinding = true // Enable View Binding
    }
}


dependencies {
    implementation ("com.squareup.okhttp3:okhttp:4.11.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2")
    implementation ("com.android.volley:volley:1.2.1")
    implementation ("org.json:json:20210307")
    implementation ("com.airbnb.android:lottie:6.1.0")
    implementation ("com.mikhaellopez:circularprogressbar:3.1.0")
    implementation ("androidx.core:core-ktx:1.12.0")
    implementation ("com.google.android.gms:play-services-maps:18.2.0")
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation ("com.google.android.material:material:1.6.0")
    implementation ("androidx.cardview:cardview:1.0.0")
    implementation("com.airbnb.android:lottie:6.1.0") // Lottie for animations
    implementation(libs.androidx.core.ktx) // Core KTX library
    implementation(libs.androidx.appcompat) // AppCompat library for backward compatibility
    implementation(libs.material) // Material Design components
    implementation(libs.androidx.activity) // Activity library
    implementation(libs.androidx.constraintlayout)
    implementation(libs.volley)
    implementation(libs.play.services.cast.framework) // ConstraintLayout for flexible UI
    testImplementation(libs.junit) // JUnit for unit testing
    androidTestImplementation(libs.androidx.junit) // JUnit for Android testing
    androidTestImplementation(libs.androidx.espresso.core) // Espresso for UI testing
}