// app/build.gradle.kts
import java.util.Properties

val localProperties = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(f.inputStream())
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.catalabytes.ekopump"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.catalabytes.ekopump"
        minSdk = 26                  // Android 8.0+ → ~95% dispositivos activos
        targetSdk = 35
        versionCode = 9
        versionName = "1.4.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Soporte de idiomas explícito
        resourceConfigurations += listOf("es", "ca", "eu", "gl", "en")
    }

    signingConfigs {
        create("release") {
            storeFile = file("/home/edeb13/ekopump-release.jks")
            storePassword = localProperties["KEYSTORE_PASSWORD"] as String
            keyAlias = "ekopump"
            keyPassword = localProperties["KEY_PASSWORD"] as String
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            buildConfigField("String", "MINETUR_BASE_URL",
                "\"https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes/\"")
            buildConfigField("Boolean", "ENABLE_LOGS", "true")
        }
        release {
            signingConfig = signingConfigs.getByName("release")          
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "MINETUR_BASE_URL",
                "\"https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes/\"")
            buildConfigField("Boolean", "ENABLE_LOGS", "false")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    // Excluir archivos duplicados en el APK final
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md"
        }
    }
}

dependencies {

    // ── Core ──────────────────────────────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // ── Jetpack Compose ───────────────────────────────────────────────────
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // ── Navegación ────────────────────────────────────────────────────────
    implementation(libs.androidx.navigation.compose)

    // ── Coroutines ────────────────────────────────────────────────────────
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.coroutines.play.services)

    // ── Hilt (Inyección de dependencias) ──────────────────────────────────
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // ── Red: Retrofit + OkHttp + Moshi ────────────────────────────────────
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.moshi)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)        // Solo en debug (ver abajo)
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.codegen)

    // ── Base de datos local: Room ──────────────────────────────────────────
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // ── Preferencias: DataStore ────────────────────────────────────────────
    implementation(libs.datastore.preferences)

    // ── Mapas: MapLibre ───────────────────────────────────────────────────
    implementation(libs.maplibre.android)
    implementation(libs.play.services.location)

    // ── Firebase ──────────────────────────────────────────────────────────
    implementation(platform(libs.firebase.bom))
    //implementation(libs.firebase.firestore)
    //implementation(libs.firebase.auth)
    implementation(libs.firebase.messaging)
    //implementation(libs.firebase.analytics)

    // ── Google Play Billing ───────────────────────────────────────────────
    implementation(libs.billing)

    // ── IA: ML Kit ────────────────────────────────────────────────────────
    implementation(libs.mlkit.translate)

    // ── Imágenes: Coil ────────────────────────────────────────────────────
    implementation(libs.coil.compose)

    // ── Gráficos de precios: Vico ─────────────────────────────────────────
    implementation(libs.vico.compose)

    // ── Testing ───────────────────────────────────────────────────────────
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test)
}
