// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    id("com.android.application") version "9.3.0" apply false
    id("com.android.library") version "9.3.0" apply false
    id("org.jetbrains.kotlin.android") version "2.4.0" apply false
    id("com.google.dagger.hilt.android") version "2.60.1" apply false
    id("com.google.devtools.ksp") version "2.3.9" apply false
    id("com.google.android.gms.oss-licenses-plugin") version "0.12.0" apply false
    id("com.google.gms.google-services") version "4.5.0" apply false
    id("com.google.firebase.crashlytics") version "3.0.7" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.9.8" apply false
}

val kotlinVersion: String by project
extra.set("kotlinVersion", kotlinVersion)

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
