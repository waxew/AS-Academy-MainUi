plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.android.compose.screenshot")
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

    experimentalProperties["android.experimental.enableScreenshotTest"] = true

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // When MainUi is consumed by a Course App, the host's pinned Core remains authoritative.
    // In standalone development the included AS-Academy-Core composite substitutes this coordinate.
    val hostCore = rootProject.findProject(":core")
    if (hostCore != null) {
        compileOnly(hostCore)
    } else {
        compileOnly("com.asdevelopers.academy:core:1.5.0")
    }

    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-compose:1.12.2")
    implementation(platform("androidx.compose:compose-bom:2025.12.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    testImplementation("junit:junit:4.13.2")
    debugImplementation("androidx.compose.ui:ui-tooling")
    screenshotTestImplementation("com.asdevelopers.academy:core:1.5.0")
    screenshotTestImplementation("com.android.tools.screenshot:screenshot-validation-api:0.0.1-alpha15")
    screenshotTestImplementation("androidx.compose.ui:ui-tooling")
}
