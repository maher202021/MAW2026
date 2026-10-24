import java.util.Properties
import java.io.FileInputStream
plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.secrets)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.hilt.android)
  alias(libs.plugins.firebase.crashlytics)
  id("com.google.gms.google-services")
  id("kotlin-parcelize")
}

android {
  namespace = "com.serviseyem"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.serviseyem"
    minSdk = 24
    targetSdk = 35
    versionCode = 5
    versionName = "1.2.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    buildConfigField("String", "SIGNATURE_HASH", "\"\"")
    buildConfigField("boolean", "IS_PAYMENT_ENABLED", "false")
    val agoraProps = Properties()
    val agoraPropsFile = rootProject.file("local.properties")
    if (agoraPropsFile.exists()) {
        agoraProps.load(FileInputStream(agoraPropsFile))
    }
    val agoraId = agoraProps.getProperty("AGORA_APP_ID") ?: System.getenv("AGORA_APP_ID") ?: ""
    buildConfigField("String", "AGORA_APP_ID", "\"$agoraId\"")
  }

  signingConfigs {
    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localProperties.load(FileInputStream(localPropertiesFile))
    }

    create("releaseConfig") {
      val ksPath = System.getenv("KEYSTORE_PATH") ?: localProperties.getProperty("KEYSTORE_PATH") ?: "${rootDir}/my-release-key.jks"
      val customKeystore = file(ksPath)
      val releaseKeystore = file("${rootDir}/release.keystore")
      val debugKeystore = file("${rootDir}/debug.keystore")
      
      val storePass: String? = System.getenv("RELEASE_KEYSTORE_PASSWORD") ?: System.getenv("STORE_PASSWORD") ?: localProperties.getProperty("RELEASE_KEYSTORE_PASSWORD")
      val keyAl: String? = System.getenv("RELEASE_KEY_ALIAS") ?: System.getenv("KEY_ALIAS") ?: localProperties.getProperty("RELEASE_KEY_ALIAS")
      val keyPass: String? = System.getenv("RELEASE_KEY_PASSWORD") ?: System.getenv("KEY_PASSWORD") ?: localProperties.getProperty("RELEASE_KEY_PASSWORD")

      if (customKeystore.exists() || releaseKeystore.exists()) {
        if (storePass == null || keyAl == null || keyPass == null) {
            throw GradleException("Release keystore passwords are not configured in local.properties or environment variables!")
        }
        storeFile = if (customKeystore.exists()) customKeystore else releaseKeystore
        storePassword = storePass
        keyAlias = keyAl
        keyPassword = keyPass
      } else if (debugKeystore.exists()) {
        storeFile = debugKeystore
        storePassword = "android"
        keyAlias = "androiddebugkey"
        keyPassword = "android"
      }
    }
    create("debugConfig") {
      val debugKeystore = file("${rootDir}/debug.keystore")
      if (debugKeystore.exists()) {
        storeFile = debugKeystore
        storePassword = "android"
        keyAlias = "androiddebugkey"
        keyPassword = "android"
      }
    }
  }

  lint {
    checkReleaseBuilds = false
    abortOnError = false
  }

  buildTypes {
    release {
      isCrunchPngs = true
      isMinifyEnabled = false
      isShrinkResources = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("releaseConfig")
    }
    debug {
      isMinifyEnabled = false
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlinOptions {
    jvmTarget = "17"
    freeCompilerArgs += listOf("-Xskip-metadata-version-check")
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions {
    unitTests {
      isIncludeAndroidResources = true
      all {
        it.jvmArgs(
          "-XX:+EnableDynamicAgentLoading",
          "-Djdk.attach.allowAttachSelf=true"
        )
      }
    }
  }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation("com.google.android.material:material:1.12.0")
  implementation("androidx.fragment:fragment-ktx:1.8.6")
  implementation(libs.androidx.security.crypto)
  implementation(platform(libs.firebase.bom))
  implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.camera.camera2)
  implementation(libs.androidx.camera.core)
  implementation(libs.androidx.camera.lifecycle)
  implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation("androidx.core:core-splashscreen:1.0.1")
  implementation(libs.hilt.android)
  ksp(libs.hilt.compiler)
  implementation(libs.androidx.hilt.navigation.compose)
  implementation(libs.androidx.work.runtime.ktx)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  // implementation(libs.androidx.room.ktx)
  // implementation(libs.androidx.room.runtime)
  // ksp(libs.androidx.room.compiler)
  implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai)
  implementation(libs.firebase.auth)
  implementation(libs.firebase.appcheck)
  implementation(libs.firebase.appcheck.playintegrity)
  implementation(libs.firebase.appcheck.debug)
  implementation(libs.firebase.firestore)
  implementation("com.google.firebase:firebase-database")
  implementation(libs.firebase.storage)
  implementation(libs.firebase.messaging)
  implementation("com.google.firebase:firebase-functions")
  implementation(libs.firebase.crashlytics)
  implementation("com.google.firebase:firebase-analytics")
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.mockk)
  testImplementation(libs.turbine)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
}

tasks.register("copyApkToRoot") {
    doLast {
        val srcFile = file("${layout.buildDirectory.get().asFile}/outputs/apk/debug/app-debug.apk")
        val destFile = file("${project.rootDir}/app-debug.apk")
        val buildOutputsDir = file("${project.rootDir}/.build-outputs")
        buildOutputsDir.mkdirs()
        val buildOutputsApk = file("${project.rootDir}/.build-outputs/app-debug.apk")
        if (srcFile.exists()) {
            srcFile.copyTo(destFile, overwrite = true)
            srcFile.copyTo(buildOutputsApk, overwrite = true)
            println("APK copied successfully to ${destFile.absolutePath} (Size: ${destFile.length()} bytes)")
        }
    }
}

afterEvaluate {
    tasks.findByName("assembleDebug")?.finalizedBy("copyApkToRoot")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xskip-metadata-version-check")
    }
}
