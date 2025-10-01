buildscript {
    dependencies {
        classpath(libs.google.services)
        classpath(libs.android.gradle.plugin)

    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.dotenv) apply false  // Add the dotenv plugin here
}