plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.helpplease"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.helpplease"
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            // Exclude conflicting META-INF files
            excludes.add("META-INF/DEPENDENCIES")
            excludes.add("META-INF/LICENSE")
            excludes.add("META-INF/LICENSE.txt")
            excludes.add("META-INF/license.txt")
            excludes.add("META-INF/NOTICE")
            excludes.add("META-INF/NOTICE.txt")
            excludes.add("META-INF/notice.txt")
            excludes.add("META-INF/ASL2.0")
            excludes.add("META-INF/*.kotlin_module")

            // Exclude signature files which cause issues with APK signature
            excludes.add("META-INF/*.SF")
            excludes.add("META-INF/*.DSA")
            excludes.add("META-INF/*.RSA")

            // If you encounter duplicate service provider configuration files
            excludes.add("META-INF/services/*")

            // Pick strategy for other duplicate cases that might occur
            pickFirsts.add("**/*.so")
            pickFirsts.add("**/*.dylib")

            // For specific problematic libraries, consider more targeted exclusions
            // This is for document processing libraries that may contain internal duplicates
            pickFirsts.add("mozilla/public-suffix-list.txt")
            pickFirsts.add("lib/arm64-v8a/**")
            pickFirsts.add("lib/armeabi-v7a/**")
            pickFirsts.add("lib/x86/**")
            pickFirsts.add("lib/x86_64/**")
        }
    }
}

// app/build.gradle.kts
dependencies {
    // Compose UI
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.ui:ui:1.5.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.0")
    implementation("androidx.compose.material3:material3:1.1.1")
// Add these to your dependencies section
    implementation("androidx.compose.material:material-icons-extended:1.5.0")
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.0")

// PDF parsing with iText7
    implementation("com.itextpdf:itext7-core:7.2.5")

// DOCX parsing with Apache POI
    implementation("org.apache.poi:poi:5.2.3")
    implementation("org.apache.poi:poi-ooxml:5.2.3")
    // Lifecycle & ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")

    // PDF parsing (for document processing)
    implementation("com.itextpdf:itext7-core:7.2.5")

    // Natural Language Processing for text analysis
    implementation("org.apache.opennlp:opennlp-tools:2.2.0")

    // Room for local database
    implementation("androidx.room:room-runtime:2.5.2")
    implementation("androidx.room:room-ktx:2.5.2")
    ksp("androidx.room:room-compiler:2.5.2")
}