// Top of the file
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt") // ADDED: For annotation processing (needed for Moshi)
}

android {
    namespace = "com.h4rsh.botify"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.h4rsh.botify"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ADDED: Your Spotify API credentials will be accessible in your code
        buildConfigField("String", "SPOTIFY_CLIENT_ID", "\"6bd51120a1f349d7b0f49d41a3ea4e50\"")
        buildConfigField("String", "SPOTIFY_CLIENT_SECRET", "\"882285f7f2294e2789c6d5f3ab5f55c7\"")
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
        buildConfig = true
    }
}

dependencies {

    // Default and Views dependencies
    implementation(libs.androidx.core.ktx)
    implementation("androidx.appcompat:appcompat:1.7.0") // CHANGED: For Views
    implementation("com.google.android.material:material:1.12.0") // CHANGED: For Views
    implementation("androidx.constraintlayout:constraintlayout:2.1.4") // CHANGED: For Views

    // Porcupine for Wake Word
    implementation("ai.picovoice:porcupine-android:3.0.2")

    // Retrofit & Moshi for Spotify Web API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.15.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Easy permissions handling
    implementation("com.guolindev.permissionx:permissionx:1.7.1")


    // Test dependencies (mostly unchanged)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // REMOVED: All Jetpack Compose dependencies
}