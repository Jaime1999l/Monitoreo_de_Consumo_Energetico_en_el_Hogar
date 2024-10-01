plugins {
    alias(libs.plugins.android.application)
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
    //noinspection GradleDependency
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}