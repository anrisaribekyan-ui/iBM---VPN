package com.v2ray.ang.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.v2ray.ang.R

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
            TextButton(
                onClick = onChooseApps,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(text = stringResource(R.string.quick_app_vpn_choose))
            }
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
            .heightIn(min = 56.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = null)
    }
}
