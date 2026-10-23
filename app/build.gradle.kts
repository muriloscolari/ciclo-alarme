import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

val customKeystorePath = System.getenv("KEYSTORE_PATH")
    ?: keystoreProperties.getProperty("storeFile")
val customKeystorePassword = System.getenv("KEYSTORE_PASSWORD")
    ?: keystoreProperties.getProperty("storePassword")
val customKeyAlias = System.getenv("KEY_ALIAS")
    ?: keystoreProperties.getProperty("keyAlias")
val customKeyPassword = System.getenv("KEY_PASSWORD")
    ?: keystoreProperties.getProperty("keyPassword")

val hasValidSigning = !customKeystorePath.isNullOrBlank() &&
        file(customKeystorePath).exists() &&
        !customKeystorePassword.isNullOrBlank() &&
        !customKeyAlias.isNullOrBlank() &&
        !customKeyPassword.isNullOrBlank()

val runNumber = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 1
val gitCommitCount = runCatching {
    val process = ProcessBuilder("git", "rev-list", "--count", "HEAD").start()
    process.inputStream.bufferedReader().readText().trim().toInt()
}.getOrDefault(1)

val computedVersionCode = if (System.getenv("GITHUB_RUN_NUMBER") != null) runNumber else (100 + gitCommitCount)
val computedVersionName = "1.0.$computedVersionCode"

android {
    namespace = "me.muriloscolari.ciclo"
    compileSdk = 35

    defaultConfig {
        applicationId = "me.muriloscolari.ciclo"
        minSdk = 26
        targetSdk = 35
        versionCode = computedVersionCode
        versionName = computedVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        if (hasValidSigning) {
            create("ciclo") {
                storeFile = file(customKeystorePath!!)
                storePassword = customKeystorePassword
                keyAlias = customKeyAlias
                keyPassword = customKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (hasValidSigning) {
                signingConfig = signingConfigs.getByName("ciclo")
            }
        }
        debug {
            if (hasValidSigning) {
                signingConfig = signingConfigs.getByName("ciclo")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    androidResources {
        generateLocaleConfig = true
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = false
        error += mutableSetOf("HardcodedText", "SetTextI18n")
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Haze (Blur / Frosted glass)
    implementation(libs.haze)
    implementation(libs.haze.materials)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Tests
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.espresso.intents)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.room.testing)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
