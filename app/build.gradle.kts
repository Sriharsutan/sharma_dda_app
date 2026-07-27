plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.assist.doorstepdocuments"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.assist.doorstepdocuments"
        minSdk = 24
        targetSdk = 35
        versionCode = 5
        versionName = "1.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Add this for 16 KB page size support
        ndk {
            //noinspection ChromeOsAbiSupport
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("dda-upload-key.jks")
//            storePassword = System.getenv("DDA_STORE_PASSWORD")
            keyAlias = "dda_key"
//            keyPassword = System.getenv("DDA_KEY_PASSWORD")
            storePassword = "Forgot@pa&&w0rd"
            keyPassword = "Forgot@pa&&w0rd"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core dependencies
    implementation("androidx.core:core-ktx:1.13.1") // Updated
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0") // Updated
    implementation("androidx.activity:activity-compose:1.9.0") // Updated
    implementation("androidx.activity:activity-ktx:1.9.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation(platform("androidx.compose:compose-bom:2024.06.00")) // Updated
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended:1.6.8") // Updated
    implementation("com.itextpdf:itextg:5.5.10")


    // Firebase - Use single BOM version
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-messaging")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7") // Updated

    // Accompanist Pager
    implementation("com.google.accompanist:accompanist-pager:0.34.0") // Updated
    implementation("com.google.accompanist:accompanist-pager-indicators:0.34.0") // Updated

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.6.0") // Updated

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}