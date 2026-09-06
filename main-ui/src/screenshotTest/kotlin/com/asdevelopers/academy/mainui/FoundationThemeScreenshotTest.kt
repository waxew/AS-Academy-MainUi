package com.asdevelopers.academy.mainui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest

@PreviewTest
@Preview(name = "Foundation Light", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun FoundationLightScreenshot() {
    FoundationScreenshotProbe(darkTheme = false)
}

@PreviewTest
@Preview(
    name = "Foundation Dark",
    showBackground = true,
    widthDp = 360,
    heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun FoundationDarkScreenshot() {
    FoundationScreenshotProbe(darkTheme = true)
}

@Composable
private fun FoundationScreenshotProbe(darkTheme: Boolean) {
    FoundationAcademyTheme(
        branding = DefaultMainUiBranding,
        darkTheme = darkTheme
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("AS Academy", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Design-system visual baseline for typography, surfaces, spacing and actions.",
                style = MaterialTheme.typography.bodyLarge
            )
            Card(Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Foundation card", style = MaterialTheme.typography.titleMedium)
                    Text("Readable content surface", style = MaterialTheme.typography.bodyMedium)
                }
            }
            Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Text("Continue")
            }
        }
    }
}
