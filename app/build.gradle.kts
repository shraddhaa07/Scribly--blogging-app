import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.scribly"
    compileSdk = 35

    val file = rootProject.file("local.properties")
    val properties = Properties()
    properties.load(FileInputStream(file))

    defaultConfig {
        applicationId = "com.example.scribly"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "cloud_name",properties.getProperty("CLOUDINARY_CLOUD_NAME"))
        buildConfigField("String", "cloud_api_key",properties.getProperty("CLOUDINARY_API_KEY"))
        buildConfigField("String", "cloud_api_secret",properties.getProperty("CLOUDINARY_API_SECRET"))

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
    buildFeatures{
        buildConfig = true // Enable BuildConfig generation
        viewBinding = true
    }
}

dependencies {

    implementation(libs.kotlin.stdlib)  // Use the reference from libs.versions.toml
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    //implementation(project(":app"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.cloudinary.android)
    implementation(libs.glide)  // Glide dependency
    annotationProcessor(libs.glideCompiler) // Glide compiler
    implementation(libs.circleimageview)  // CircleImageView dependency



}