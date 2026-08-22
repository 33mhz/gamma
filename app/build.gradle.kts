import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileInputStream
import java.util.*
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val hasGoogleServices = file("google-services.json").exists()

plugins {
  id("com.android.application")
  id("com.google.devtools.ksp")
  id("kotlin-parcelize")
  id("com.google.android.gms.oss-licenses-plugin")
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
  description = "Generate Jacoco coverage reports for tests"
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

val versionPropsFile = file("version.properties")
val versionProps = Properties()
if (versionPropsFile.exists()) {
  versionProps.load(FileInputStream(versionPropsFile))
}

val currentVersionCode = versionProps.getProperty("versionCode", "1").toInt()

android {
  namespace = "io.pnut.gamma"
  ndkVersion = "26.1.10909125"
  compileSdk = 37

  defaultConfig {
    applicationId = "io.pnut.gamma"
    minSdk = 30
    targetSdk = 37
    versionCode = currentVersionCode
    versionName = "0.10.1"
    testInstrumentationRunner = "io.pnut.gamma.HiltTestRunner"
  }
  buildTypes {
    release {
      signingConfig = signingConfigs.getByName("debug")
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
    debug {
      enableUnitTestCoverage = true
      enableAndroidTestCoverage = true
    }
  }
  buildFeatures {
    viewBinding = true
    dataBinding = false
    buildConfig = true
  }
  val keystorePropertiesFile = rootProject.file("keystore.properties")
  if (keystorePropertiesFile.exists()) {
    val keystoreProperties = Properties()
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
      pickFirsts.add("mockito-extensions/org.mockito.plugins.MockMaker")
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
  androidTestImplementation("androidx.arch.core:core-testing:2.2.0")

  implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
  implementation("androidx.constraintlayout:constraintlayout:2.2.2")
  implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0")

  implementation("com.google.android.material:material:1.14.0")
  implementation("androidx.appcompat:appcompat:1.8.0")
  implementation("androidx.recyclerview:recyclerview:1.4.0")
  implementation("com.squareup.retrofit2:retrofit:3.0.0")
  implementation("com.squareup.retrofit2:converter-moshi:3.0.0")
  implementation("androidx.preference:preference-ktx:1.2.1")
  implementation("com.github.chrisbanes:PhotoView:2.3.0")


  val moshiVersion = "1.15.2"
  implementation("com.squareup.moshi:moshi:$moshiVersion")
  implementation("com.squareup.moshi:moshi-adapters:$moshiVersion")
  ksp("com.squareup.moshi:moshi-kotlin-codegen:$moshiVersion")

  implementation("com.github.bumptech.glide:glide:5.0.9")

  val coroutinesVersion = "1.11.0"
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

  implementation("androidx.palette:palette-ktx:1.0.0")
  implementation("androidx.browser:browser:1.10.0")
  implementation("androidx.emoji2:emoji2:1.6.0")
  implementation("androidx.emoji2:emoji2-bundled:1.6.0")

  implementation("com.squareup.okhttp3:logging-interceptor:5.5.0")

  val daggerVersion = "2.60.1"
  implementation("com.google.dagger:dagger:$daggerVersion")
  implementation("com.google.dagger:hilt-android:$daggerVersion")
  ksp("com.google.dagger:hilt-android-compiler:$daggerVersion")
  androidTestImplementation("com.google.dagger:hilt-android-testing:$daggerVersion")

  implementation("com.github.CanHub:Android-Image-Cropper:4.5.0")

  implementation("com.google.android.gms:play-services-oss-licenses:17.5.1")

  if (hasGoogleServices) {
    implementation(platform("com.google.firebase:firebase-bom:34.17.0"))
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")
  }

  testImplementation("junit:junit:4.13.2")
  testImplementation("com.google.truth:truth:1.4.5")
  testImplementation("org.mockito:mockito-core:5.23.0")
  testImplementation("org.mockito.kotlin:mockito-kotlin:6.3.0")
  androidTestImplementation("org.mockito:mockito-android:5.23.0")
  testImplementation("org.robolectric:robolectric:4.16.1")
  val espressoVersion = "3.7.0"
  androidTestImplementation("androidx.test.espresso:espresso-core:$espressoVersion")
  androidTestImplementation("androidx.test.espresso:espresso-intents:$espressoVersion")
  val testVersion = "1.7.0"
  androidTestImplementation("androidx.test.ext:truth:$testVersion")
  androidTestImplementation("androidx.test:core:$testVersion")
  androidTestImplementation("androidx.test:rules:$testVersion")
  androidTestImplementation("androidx.test.ext:junit:1.3.0")

  // Kotlin
  val navVersion = "2.9.8"
  implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
  implementation("androidx.navigation:navigation-ui-ktx:$navVersion")

  val roomVersion = "2.8.4"
  implementation("androidx.room:room-runtime:$roomVersion")
  implementation("androidx.room:room-ktx:$roomVersion")
  ksp("androidx.room:room-compiler:$roomVersion")

  val workVersion = "2.11.2"
  implementation("androidx.work:work-runtime-ktx:$workVersion")
  implementation("androidx.hilt:hilt-work:1.4.0")
  ksp("androidx.hilt:hilt-compiler:1.4.0")
  implementation("androidx.startup:startup-runtime:1.2.0")
}

if (hasGoogleServices) {
  pluginManager.apply("com.google.gms.google-services")
  pluginManager.apply("com.google.firebase.crashlytics")
}
