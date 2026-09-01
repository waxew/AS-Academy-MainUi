plugins {
    // Versions intentionally track the current AS-Academy-Core toolchain.
    id("com.android.application") version "9.2.1" apply false
    id("com.android.library") version "9.2.1" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.10" apply false
}

allprojects {
    group = "com.asdevelopers.academy"
    version = "0.1.0"
}
