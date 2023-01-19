plugins {
    id("com.android.library")
    kotlin("android")
    id("tachiyomi.lint")
    kotlin("plugin.serialization")
    id("com.github.ben-manes.versions")
}

android {
    namespace = "eu.kanade.tachiyomi.source"
    compileSdk = AndroidConfig.compileSdk

    defaultConfig {
        minSdk = AndroidConfig.minSdk
        targetSdk = AndroidConfig.targetSdk
        consumerProguardFile("consumer-proguard.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

dependencies {

    implementation(project(":core"))

    api(kotlinx.serialization.json)

    api(libs.rxjava)

    api(libs.preferencektx)

    api(libs.jsoup)

    implementation(androidx.corektx)

    // SY -->
    implementation(project(":i18n"))
    implementation(kotlinx.reflect)
    // SY <--
}
