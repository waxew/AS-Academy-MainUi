package com.asdevelopers.academy.mainui

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color.parseColor
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.asdevelopers.academy.core.settings.AcademyProfile
import com.asdevelopers.academy.course.model.CourseBranding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

val DefaultMainUiBranding = CourseBranding(
    primaryColorHex = "#6750A4",
    secondaryColorHex = "#625B71",
    accentColorHex = "#7D5260"
)

@Composable
fun AcademyMainUiTheme(
    branding: CourseBranding = DefaultMainUiBranding,
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val primary = branding.primaryColorHex.toComposeColor(Color(0xFF6750A4))
    val secondary = branding.secondaryColorHex.toComposeColor(Color(0xFF625B71))
    val tertiary = branding.accentColorHex.toComposeColor(Color(0xFF7D5260))
    val colors = if (darkTheme) {
        darkColorScheme(primary = primary, secondary = secondary, tertiary = tertiary)
    } else {
        lightColorScheme(primary = primary, secondary = secondary, tertiary = tertiary)
    }
    MaterialTheme(colorScheme = colors, content = content)
}

data class AcademyMainUiDrawerItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val selected: Boolean = false,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademyMainUiShell(
    title: String,
    profile: AcademyProfile,
    courseItems: List<AcademyMainUiDrawerItem>,
    onProfileImageClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onShareClick: () -> Unit,
    onAboutClick: () -> Unit,
    contentIsRtl: Boolean = true,
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val closeDrawerThen: (() -> Unit) -> Unit = { action ->
        scope.launch { drawerState.close(); action() }
    }
    AcademyMainUiDrawerLayout(
        drawerState = drawerState,
        profile = profile,
        courseItems = courseItems.map { item -> item.copy(onClick = { closeDrawerThen(item.onClick) }) },
        onProfileImageClick = onProfileImageClick,
        onSettingsClick = { closeDrawerThen(onSettingsClick) },
        onShareClick = { closeDrawerThen(onShareClick) },
        onAboutClick = { closeDrawerThen(onAboutClick) },
        contentIsRtl = contentIsRtl
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Outlined.Menu, contentDescription = "باز کردن منو")
                        }
                    }
                )
            },
            content = content
        )
    }
}

@Composable
private fun AcademyMainUiDrawerLayout(
    drawerState: DrawerState,
    profile: AcademyProfile,
    courseItems: List<AcademyMainUiDrawerItem>,
    onProfileImageClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onShareClick: () -> Unit,
    onAboutClick: () -> Unit,
    contentIsRtl: Boolean,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(modifier = Modifier.width(320.dp).fillMaxHeight()) {
                    DrawerProfileHeader(profile, onProfileImageClick)
                    HorizontalDivider()
                    NavigationDrawerItem(
                        label = { Text("تنظیمات") },
                        selected = false,
                        icon = { Icon(Icons.Outlined.Settings, null) },
                        onClick = onSettingsClick,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    NavigationDrawerItem(
                        label = { Text("اشتراک‌گذاری با دوستان") },
                        selected = false,
                        icon = { Icon(Icons.Outlined.Share, null) },
                        onClick = onShareClick,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    if (courseItems.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        courseItems.forEach { item ->
                            NavigationDrawerItem(
                                label = { Text(item.label) },
                                selected = item.selected,
                                icon = { Icon(item.icon, null) },
                                onClick = item.onClick,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    HorizontalDivider()
                    NavigationDrawerItem(
                        label = { Text("درباره نرم‌افزار") },
                        selected = false,
                        icon = { Icon(Icons.Outlined.Info, null) },
                        onClick = onAboutClick,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        ) {
            val direction = if (contentIsRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
            CompositionLocalProvider(LocalLayoutDirection provides direction, content = content)
        }
    }
}

@Composable
private fun DrawerProfileHeader(profile: AcademyProfile, onProfileImageClick: () -> Unit) {
    val context = LocalContext.current
    val profileBitmap by produceState<ImageBitmap?>(null, profile.imageUri) {
        value = withContext(Dispatchers.IO) {
            profile.imageUri?.let { rawUri -> runCatching { context.decodeProfileBitmap(Uri.parse(rawUri)) }.getOrNull() }
        }
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier.size(92.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape).clickable(onClick = onProfileImageClick),
            contentAlignment = Alignment.Center
        ) {
            if (profileBitmap == null) {
                Icon(Icons.Outlined.Person, "انتخاب تصویر پروفایل", Modifier.size(46.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
            } else {
                Image(requireNotNull(profileBitmap), "تصویر پروفایل", contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize().clip(CircleShape))
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Person, null, Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(profile.displayName, fontWeight = FontWeight.Bold)
        }
        Text("برای تغییر تصویر لمس کنید", style = MaterialTheme.typography.bodySmall)
    }
}

private fun String.toComposeColor(fallback: Color): Color = runCatching { Color(parseColor(this)) }.getOrDefault(fallback)

private fun Context.decodeProfileBitmap(uri: Uri): ImageBitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    var sampleSize = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / sampleSize > 512) sampleSize *= 2
    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    return contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options)?.asImageBitmap() }
}
