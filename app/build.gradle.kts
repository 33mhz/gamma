import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileInputStream
import java.util.*
import org.jetbrains.kotlin.gradle.dsl.JvmTarget


plugins {
  id("com.android.application")
//  id("org.jetbrains.kotlin.android")
  id("com.google.devtools.ksp")
  id("kotlin-parcelize")
  id("com.google.android.gms.oss-licenses-plugin")
  id("com.google.firebase.crashlytics")
  id("jacoco")
  id("com.google.dagger.hilt.android")
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
  }
}

jacoco {
  toolVersion = "0.8.12"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest", "connectedDebugAndroidTest", "createDebugCoverageReport")
  reports {
    xml.required.set(true)
    csv.required.set(false)
    html.required.set(true)
  }
  sourceDirectories.setFrom(files(project.projectDir.resolve("src/main/java")))
  classDirectories.setFrom(files(project.layout.buildDirectory.dir("tmp/kotlin-classes/debug")))
  executionData.setFrom(fileTree(project.layout.buildDirectory) {
    include(
      "jacoco/testDebugUnitTest.exec",
      "outputs/code_coverage/debugAndroidTest/connected/*coverage.ec"
    )
  })
}

android {
  namespace = "net.unsweets.gamma"
  ndkVersion = "26.1.10909125"
  compileSdk = 37
  defaultConfig {
    applicationId = "net.unsweets.gamma"
    minSdk = 33
    targetSdk = 37
    versionCode = 6
    versionName = "0.5.0"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    renderscriptTargetApi = 33
    renderscriptSupportModeEnabled = true
  }
  buildTypes {
    getByName("release") {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
    getByName("debug") {
      enableUnitTestCoverage = true
      enableAndroidTestCoverage = true
    }
  }
  buildFeatures {
    viewBinding = true
    dataBinding = true
    buildConfig = true
  }
  val keystorePropertiesFile = rootProject.file("keystore.properties")
  if (keystorePropertiesFile.exists()) {
    // Create a variable called keystorePropertiesFile, and initialize it to your
    // keystore.properties file, in the rootProject folder.

    // Initialize a new Properties() object called keystoreProperties.
    val keystoreProperties = Properties()

    // Load your keystore.properties file into the keystoreProperties object.
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
    signingConfigs {
      create("release") {
        keyAlias = keystoreProperties.getProperty("keyAlias")
        keyPassword = keystoreProperties.getProperty("keyPassword")
        storeFile = File(keystoreProperties.getProperty("storeFile"))
        storePassword = keystoreProperties.getProperty("storePassword")
      }
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  lint {
    abortOnError = false
  }
  testOptions {
    unitTests.isIncludeAndroidResources = false
    unitTests.isReturnDefaultValues = true
  }
  packaging {
    resources {
      // https://github.com/mockito/mockito/issues/1376#issuecomment-391192483
      pickFirsts.add("mockito-extensions/org.mockito.plugins.MockMaker")
    }
  }

  sourceSets {
    val sharedTestDir = "src/sharedTest/java"
    getByName("test") {
      java.directories.add(sharedTestDir)
    }
    getByName("androidTest") {
      java.directories.add(sharedTestDir)
    }
  }
}

tasks.withType<KotlinCompile>().configureEach {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_17)
  }
}
val kotlinVersion = extra["kotlinVersion"] as String

dependencies {
  implementation(fileTree(mapOf("dir" to "libs", "include" to arrayOf("*.jar"))))

  val lifecycleVersion = "2.11.0"
  implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
  implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
  implementation("androidx.lifecycle:lifecycle-common-java8:$lifecycleVersion")
  androidTestImplementation("androidx.arch.core:core-testing:2.2.0")

  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion")
  implementation("androidx.constraintlayout:constraintlayout:2.2.1")
  implementation("androidx.legacy:legacy-support-v4:1.0.0")

  implementation("com.google.android.material:material:1.14.0")
  implementation("androidx.appcompat:appcompat:1.7.1")
  implementation("androidx.recyclerview:recyclerview:1.3.2")
  implementation("com.squareup.retrofit2:retrofit:2.11.0")
  implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
  implementation("androidx.preference:preference-ktx:1.2.1")
  implementation("com.github.natario1:NestedScrollCoordinatorLayout:1.0.3")
  implementation("com.github.chrisbanes:PhotoView:2.3.0")
  implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")


  val moshiVersion = "1.15.2"
  implementation("com.squareup.moshi:moshi:$moshiVersion")
  implementation("com.squareup.moshi:moshi-adapters:$moshiVersion")
  implementation("com.squareup.moshi:moshi-kotlin:$moshiVersion")
  ksp("com.squareup.moshi:moshi-kotlin-codegen:$moshiVersion")

  val glideVersion = "4.16.0"
  implementation("com.github.bumptech.glide:glide:$glideVersion")

  val coroutinesVersion = "1.8.1"
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

  implementation("androidx.palette:palette-ktx:1.0.0")
  implementation("androidx.browser:browser:1.8.0")
  implementation("androidx.emoji:emoji:1.2.0")
  implementation("androidx.emoji:emoji-bundled:1.2.0")

  implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

  val daggerVersion = "2.60.1"
  implementation("com.google.dagger:dagger:$daggerVersion")
  implementation("com.google.dagger:hilt-android:$daggerVersion")
  ksp("com.google.dagger:dagger-compiler:$daggerVersion")
  ksp("com.google.dagger:hilt-android-compiler:$daggerVersion")

  implementation("com.github.CanHub:Android-Image-Cropper:4.5.0")
  implementation("com.github.thefuntasty.hauler:library:2.0.0")

  implementation("com.google.android.gms:play-services-oss-licenses:17.1.0")

  implementation("com.google.firebase:firebase-analytics-ktx:22.5.0")
  implementation("com.google.firebase:firebase-crashlytics-ktx:19.4.4")

  implementation("jp.wasabeef:glide-transformations:4.3.0")
  implementation("me.zhanghai.android.materialprogressbar:library:1.6.1")

  testImplementation("junit:junit:4.13.2")
  testImplementation("org.mockito:mockito-core:5.12.0")
  testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
  androidTestImplementation("org.mockito:mockito-android:5.12.0")
  testImplementation("org.robolectric:robolectric:4.13")
  val espressoVersion = "3.6.1"
  androidTestImplementation("androidx.test.espresso:espresso-core:$espressoVersion")
  androidTestImplementation("androidx.test.espresso:espresso-intents:$espressoVersion")
  val testVersion = "1.6.1"
  androidTestImplementation("androidx.test:core:$testVersion")
  androidTestImplementation("androidx.test:rules:$testVersion")
  androidTestImplementation("androidx.test.ext:junit:1.2.1")
  androidTestImplementation("androidx.test.ext:truth:1.6.0")
  androidTestImplementation("com.google.truth:truth:1.4.5")
  testImplementation("org.powermock:powermock-module-junit4:2.0.9")
  testImplementation("org.powermock:powermock-api-mockito2:2.0.9")
  implementation("com.github.Chrisvin:EasyReveal:1.2") {
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-android-extensions-runtime")
  }
  // Kotlin
  val navVersion = "2.7.7"
  implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
  implementation("androidx.navigation:navigation-ui-ktx:$navVersion")
}

if (file("google-services.json").exists()) {
  apply(plugin = "com.google.gms.google-services")
}

// Remove androidExtensions and apply plugin google-services
