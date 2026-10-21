import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Cargar las propiedades desde app/firebase.properties si existe
val firebaseProps = Properties()
val firebasePropsFile = rootProject.file("app/firebase.properties")
if (firebasePropsFile.exists()) {
    firebaseProps.load(FileInputStream(firebasePropsFile))
}

android {
    namespace = "co.edu.unal.tictactoe"
    compileSdk = 37

    defaultConfig {
        applicationId = "co.edu.unal.tictactoe"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Inyectar variables desde firebase.properties (o usar valores por defecto)
        buildConfigField(
            "String",
            "FIREBASE_DATABASE_URL",
            "\"${firebaseProps.getProperty("FIREBASE_DATABASE_URL", "https://triquionline-51313-default-rtdb.firebaseio.com/")}\""
        )
        buildConfigField(
            "String",
            "FIREBASE_APP_ID",
            "\"${firebaseProps.getProperty("FIREBASE_APP_ID", "1:100000000000:android:1000000000000000")}\""
        )
        buildConfigField(
            "String",
            "FIREBASE_PROJECT_ID",
            "\"${firebaseProps.getProperty("FIREBASE_PROJECT_ID", "tictactoe-online-default")}\""
        )
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true // NECESARIO para permitir la generación de la clase BuildConfig
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.8.0")
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.database)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}