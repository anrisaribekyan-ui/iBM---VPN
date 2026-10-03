package com.v2ray.ang.ui.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.v2ray.ang.R
import com.v2ray.ang.ui.compose.AppDivider
import com.v2ray.ang.ui.compose.verticalScrollbar

enum class MainDestination(@DrawableRes val iconRes: Int, @StringRes val labelRes: Int) {
    Subscriptions(R.drawable.ic_subscriptions_24dp, R.string.title_sub_setting),
    PerAppProxy(R.drawable.ic_per_apps_24dp, R.string.per_app_proxy_settings),
    Routing(R.drawable.ic_routing_24dp, R.string.routing_settings_title),
    UserAssets(R.drawable.ic_file_24dp, R.string.title_user_asset_setting),
    Settings(R.drawable.ic_settings_24dp, R.string.title_settings),
    Promotion(R.drawable.ic_promotion_24dp, R.string.title_pref_promotion),
    Logcat(R.drawable.ic_logcat_24dp, R.string.title_logcat),
    CheckUpdate(R.drawable.ic_check_update_24dp, R.string.update_check_for_update),
    BackupRestore(R.drawable.ic_restore_24dp, R.string.title_configuration_backup_restore),
    About(R.drawable.ic_about_24dp, R.string.title_about)
}

/** Quick actions at the top of the drawer; they replace the controls removed from the main screen. */
enum class MainDrawerAction(@DrawableRes val iconRes: Int, @StringRes val labelRes: Int) {
    UpdateSubscriptions(R.drawable.ic_cloud_download_24dp, R.string.ibm_update_subscription),
    ImportClipboard(R.drawable.ic_copy, R.string.ibm_import_clipboard),
    Import(R.drawable.ic_add_24dp, R.string.ibm_import),
    Servers(R.drawable.ic_shield_24dp, R.string.ibm_servers)
}

private val primaryDrawerItems = listOf(
    MainDestination.Subscriptions,
    MainDestination.PerAppProxy,
    MainDestination.Routing,
    MainDestination.UserAssets,
    MainDestination.Settings
)

// iBM: upstream promotion and v2rayNG self-update entries are hidden in this build.
private val drawerItems = primaryDrawerItems + listOf(
    MainDestination.Logcat,
    MainDestination.BackupRestore,
    MainDestination.About
)

@Composable
fun MainDrawerContent(
    drawerState: DrawerState,
    onAction: (MainDrawerAction) -> Unit,
    onNavigate: (MainDestination) -> Unit
) {
    val drawerScrollState = rememberScrollState()

    ModalDrawerSheet(
        drawerState = drawerState,
        modifier = Modifier.fillMaxWidth(0.78f),
        drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        drawerShape = RoundedCornerShape(topEnd = 30.dp, bottomEnd = 30.dp)
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(drawerScrollState)
                .verticalScrollbar(drawerScrollState)
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ic_launcher is an adaptive icon (not loadable by painterResource); compose it
                // from the raster foreground on the brand background instead.
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.mipmap.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier.size(52.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            MainDrawerAction.entries.forEach { item ->
                DrawerRow(iconRes = item.iconRes, labelRes = item.labelRes, onClick = { onAction(item) })
            }
            AppDivider(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
            drawerItems.forEachIndexed { index, item ->
                if (index == primaryDrawerItems.size) {
                    AppDivider(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
                }
                DrawerRow(iconRes = item.iconRes, labelRes = item.labelRes, onClick = { onNavigate(item) })
            }
        }
    }
}

@Composable
private fun DrawerRow(
    @DrawableRes iconRes: Int,
    @StringRes labelRes: Int,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = { Text(stringResource(labelRes), style = MaterialTheme.typography.bodyLarge) },
        selected = false,
        onClick = onClick,
        icon = {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painterResource(iconRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(19.dp)
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        colors = NavigationDrawerItemDefaults.colors(
            unselectedContainerColor = Color.Transparent,
            unselectedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.padding(horizontal = 12.dp)
    )
}
