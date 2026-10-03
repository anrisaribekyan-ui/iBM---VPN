package com.v2ray.ang.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.v2ray.ang.R
import com.v2ray.ang.ui.compose.AppSwitch

/**
 * YOUdkinVPN quick settings shown directly under the connect button:
 * bypass Russian sites and per-app VPN with a shortcut to the app picker.
 */
@Composable
fun MainQuickSettings(
    ruBypassEnabled: Boolean,
    perAppProxyEnabled: Boolean,
    onAction: (MainAction) -> Unit,
    onChooseApps: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        QuickSwitchRow(
            title = stringResource(R.string.quick_ru_bypass),
            summary = stringResource(
                if (ruBypassEnabled) R.string.quick_ru_bypass_on else R.string.quick_ru_bypass_off
            ),
            checked = ruBypassEnabled,
            onCheckedChange = { onAction(MainAction.SetRuBypass(it)) },
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            QuickSwitchRow(
                title = stringResource(R.string.quick_app_vpn),
                summary = stringResource(
                    if (perAppProxyEnabled) R.string.quick_app_vpn_on else R.string.quick_app_vpn_off
                ),
                checked = perAppProxyEnabled,
                onCheckedChange = { onAction(MainAction.SetPerAppProxy(it)) },
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(R.string.quick_app_vpn_choose),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(end = 14.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                    .clickable(role = Role.Button, onClick = onChooseApps)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
    }
}

/** One focusable row: label, state summary and switch share a single toggle action. */
@Composable
private fun QuickSwitchRow(
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        AppSwitch(checked = checked, onCheckedChange = null)
    }
}
