plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.asdevelopers.academy.mainui"
    compileSdk = 36

    defaultConfig {
        minSdk = 23
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // وقتی MainUi داخل Course App مصرف می‌شود، همان Core pin‌شده Host را استفاده می‌کند.
    // در توسعه standalone می‌توان Core منتشرشده را جایگزین کرد.
    val hostCore = rootProject.findProject(":core")
    if (hostCore != null) {
        compileOnly(hostCore)
    } else {
        compileOnly("com.asdevelopers.academy:core:1.3.0")
    }

    implementation("androidx.core:core-ktx:1.17.0")
    implementation(platform("androidx.compose:compose-bom:2025.12.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
