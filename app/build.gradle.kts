import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

val localProps = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.fifa.geminiusage"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.fifa.geminiusage"
        minSdk = 26
        targetSdk = 37
        versionCode = 3
        versionName = "1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val keystorePath = localProps.getProperty("KEYSTORE_FILE") ?: ""
    val keystorePassword = localProps.getProperty("KEYSTORE_PASSWORD") ?: ""
    val keystoreAlias = localProps.getProperty("KEY_ALIAS") ?: ""
    val keystoreKeyPassword = localProps.getProperty("KEY_PASSWORD") ?: ""
    val hasReleaseKeystore = keystorePath.isNotEmpty() && file(keystorePath).exists()

    if (hasReleaseKeystore) {
        signingConfigs {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = keystorePassword
                keyAlias = keystoreAlias
                keyPassword = keystoreKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            // ใช้กุญแจตัวเดียวกับ release เพื่อให้กด Run ทับแอปเดิมและคง Session ล็อกอินไว้
            if (hasReleaseKeystore) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        release {
            if (hasReleaseKeystore) {
                signingConfig = signingConfigs.getByName("release")
            }
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

// ตั้งชื่อไฟล์ APK อัตโนมัติตาม versionName (รองรับ AGP 9.5.2)
androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            if (output is com.android.build.api.variant.impl.VariantOutputImpl) {
                output.outputFileName.set("GeminiUsage-v${output.versionName.orNull ?: "1.0"}.apk")
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}