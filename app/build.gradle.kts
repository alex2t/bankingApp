plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.firebase-perf")
    id("com.google.firebase.crashlytics")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlinx.serialization)
}

android {
    namespace = "com.example.atmosphere"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.atmosphere"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Navigation Component for Compose
    implementation(libs.navigation.compose)

    // Jetpack Compose BOM for version management
    implementation(platform(libs.androidx.compose.bom))

    // Material3 Design components
    implementation(libs.androidx.material3)

    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)


    // Core and Lifecycle libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.okhttp)

    // Compose runtime livedata
    implementation(libs.compose.runtime.livedata)

    // Jetpack Compose UI components
    implementation(libs.androidx.compose.ui)            // Core Compose UI components
    implementation(libs.androidx.ui.graphics)           // Compose graphics
    implementation(libs.androidx.ui.tooling.preview)    // Tooling for Compose previews
    implementation(libs.androidx.compose.ui.text)

    // Add foundation layout for PaddingValues
    implementation(libs.androidx.compose.foundation)

    // Lifecycle ViewModel and Compose integration
    implementation(libs.lifecycle.viewmodel.compose)


    implementation(libs.kotlinx.serialization.json)
    // WorkManager for background tasks
    implementation(libs.work.runtime.ktx)
    implementation (libs.play.services.location)


    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Debugging and preview tooling for Compose
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation(libs.compose.tooling)
    debugImplementation(libs.compose.test.manifest)

    // LeakCanary for memory leak detection
    debugImplementation(libs.leakcanary.android)


    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.perf)
    implementation(libs.firebase.crashlytics)

}
