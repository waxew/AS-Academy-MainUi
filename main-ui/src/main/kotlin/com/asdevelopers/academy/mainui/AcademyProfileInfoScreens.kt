package com.asdevelopers.academy.mainui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/** Local profile editor backed by AcademyPreferencesRepository; no remote identity is implied. */
@Composable
fun AcademyProfileScreen(
    runtime: AcademyMainUiRuntime,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val profile by runtime.preferencesRepository.profile.collectAsState(
        initial = com.asdevelopers.academy.core.settings.AcademyProfile()
    )
    var displayName by rememberSaveable { mutableStateOf(profile.displayName) }

    LaunchedEffect(profile.displayName) {
        displayName = profile.displayName
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            scope.launch {
                runtime.preferencesRepository.updateProfile(displayName, uri.toString())
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Button(onClick = onBack) { Text("بازگشت") } }
        item { Text("پروفایل", style = MaterialTheme.typography.headlineMedium) }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("نام نمایشی") }
                    )
                    Text(
                        if (profile.imageUri.isNullOrBlank()) "تصویر پروفایل انتخاب نشده" else "تصویر پروفایل ذخیره شده است",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedButton(
                        onClick = { imagePicker.launch(arrayOf("image/*")) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (profile.imageUri.isNullOrBlank()) "انتخاب تصویر" else "تغییر تصویر")
                    }
                    if (!profile.imageUri.isNullOrBlank()) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    runtime.preferencesRepository.updateProfile(displayName, null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("حذف تصویر")
                        }
                    }
                    Button(
                        enabled = displayName.isNotBlank(),
                        onClick = {
                            scope.launch {
                                runtime.preferencesRepository.updateProfile(displayName, profile.imageUri)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("ذخیره پروفایل")
                    }
                }
            }
        }
    }
}

@Composable
fun AcademyAppInfoScreen(
    appTitle: String,
    appInfo: AcademyAppInfo,
    onShare: (() -> Unit)?,
    onOpenUpdate: (() -> Unit)?,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Button(onClick = onBack) { Text("بازگشت") } }
        item { Text("درباره نرم‌افزار", style = MaterialTheme.typography.headlineMedium) }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(appTitle, style = MaterialTheme.typography.titleLarge)
                    if (appInfo.description.isNotBlank()) Text(appInfo.description)
                    if (appInfo.versionName.isNotBlank()) Text("نسخه ${appInfo.versionName}")
                    Text("پشتیبانی: ${appInfo.supportEmail}")
                }
            }
        }
        if (onShare != null) {
            item {
                Button(onClick = onShare, modifier = Modifier.fillMaxWidth()) {
                    Text("اشتراک‌گذاری")
                }
            }
        }
        if (onOpenUpdate != null) {
            item {
                Button(onClick = onOpenUpdate, modifier = Modifier.fillMaxWidth()) {
                    Text("بررسی / دریافت به‌روزرسانی")
                }
            }
        }
    }
}
