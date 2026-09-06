package com.asdevelopers.academy.thinapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.asdevelopers.academy.course.model.CourseBranding
import com.asdevelopers.academy.mainui.AcademyAppInfo
import com.asdevelopers.academy.mainui.AcademyCapability
import com.asdevelopers.academy.mainui.AcademyMainUi
import com.asdevelopers.academy.mainui.AcademyMainUiConfig

/**
 * The only app-owned Academy configuration. Runtime logic belongs to Core,
 * visual behavior belongs to MainUi, and curriculum belongs to MainCourse.
 */
object ThinAcademyAppConfig {
    const val COURSE_ID = "basic"

    val mainUi = AcademyMainUiConfig(
        courseId = COURSE_ID,
        branding = CourseBranding(
            primaryColorHex = "#1E5EFF",
            secondaryColorHex = "#173B7A",
            accentColorHex = "#00A884"
        ),
        darkTheme = false,
        typedCapabilities = setOf(
            AcademyCapability.SEARCH,
            AcademyCapability.PROGRESS,
            AcademyCapability.SETTINGS
        ),
        appInfo = AcademyAppInfo(
            versionName = "0.1.0",
            description = "Reference thin AS Academy application"
        )
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ThinAcademyApp() }
    }
}

@Composable
private fun ThinAcademyApp() {
    AcademyMainUi(config = ThinAcademyAppConfig.mainUi) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "AS Academy",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Course: ${ThinAcademyAppConfig.COURSE_ID}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "This host owns identity/config only; Core, MainUi and MainCourse own the platform.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
