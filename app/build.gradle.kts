plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.monitoreo_de_consumo_energtico_en_el_hogar"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.monitoreo_de_consumo_energtico_en_el_hogar"
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
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation (libs.google.firebase.firestore)
    implementation(libs.work.runtime.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.work.runtime)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.database)
    implementation ("com.github.skydoves:colorpickerview:2.2.4")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}