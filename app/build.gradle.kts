plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.finalproject"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.finalproject"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Firebase (sử dụng BOM)
    implementation(platform(libs.firebase.bom))  // BOM phải được thêm bằng 'platform'
    implementation(libs.firebase.database)       // Không cần version
    implementation(libs.firebase.common)         // Không cần version
    implementation(libs.firebase.firestore)      // Không cần version
    implementation(libs.firebase.auth)
    implementation(libs.firebase.analytics)
    implementation(libs.picasso)
    implementation(libs.google.services)
    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.video)
    implementation(libs.camera.view)
    implementation(libs.camera.extensions)
    implementation(libs.guava)
    implementation(libs.recyclerview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.glide)
    implementation(libs.volley)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.json)
    implementation(libs.okhttp)
    implementation(libs.androidx.viewpager2)           // Không cần version

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}