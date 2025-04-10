plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id ("kotlin-parcelize")
}

android {
    namespace = "com.foodie.foodieapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.foodie.foodieapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    viewBinding {
        enable = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        dataBinding = true
    }
}

dependencies {

    // Core & AppCompat
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat) // Now defined in TOML
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // UI - Views System
    implementation("com.google.android.material:material:1.11.0")
    // Now defined in TOML
    implementation(libs.androidx.constraintlayout) // Now defined in TOML
    implementation(libs.androidx.activity.ktx) // Now defined in TOML (base Activity)

    // UI - Jetpack Compose (If you are using Compose)
    implementation(libs.androidx.activity.compose) // For Compose in Activities
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Navigation (Views)
    implementation(libs.androidx.navigation.fragment.ktx) // Now defined in TOML
    implementation(libs.androidx.navigation.ui.ktx) // Now defined in TOML

    // Networking - Retrofit
    implementation(libs.squareup.retrofit2) // Now defined in TOML
    implementation(libs.squareup.converter.gson) // Now defined in TOML

    // Specific Libraries
    implementation("com.github.denzcoskun:ImageSlideshow:0.1.2")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation(libs.material)
    implementation(libs.androidx.activity)

    // Testing - Unit Tests
    testImplementation(libs.junit)

    // Testing - Android Instrumented Tests (Views)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Testing - Android Instrumented Tests (Compose)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Debugging (Compose)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.0")




}