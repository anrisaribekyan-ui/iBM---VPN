package com.v2ray.ang.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.v2ray.ang.R
import com.v2ray.ang.ui.compose.glassSurface
import dev.chrisbanes.haze.HazeState

private val PanelCardShape = RoundedCornerShape(26.dp)

/**
 * Single glass card with the currently selected server. Tapping it opens the full server list.
 * [delayMillis] is the last test result (0 when the server was never tested).
 */
@Composable
fun MainServerCard(
    hazeState: HazeState,
    serverName: String,
    delayMillis: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .glassSurface(hazeState, PanelCardShape)
            .clip(PanelCardShape)
            .clickable(
                onClickLabel = stringResource(R.string.ibm_choose_server),
                role = Role.Button,
                onClick = onClick
            )
            .padding(start = 20.dp, end = 12.dp, top = 14.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.ibm_server_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Text(
                text = serverName.ifBlank { stringResource(R.string.status_no_server) },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (delayMillis != 0L) {
            Spacer(Modifier.width(12.dp))
            PingLabel(delayMillis)
        }
        Spacer(Modifier.width(8.dp))
        Icon(
            painter = painterResource(R.drawable.ic_expand_more_24dp),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(24.dp)
                .rotate(-90f)
        )
    }
}

/** Glass card with the two quick toggles: bypass .ru and per-app VPN. */
@Composable
fun MainQuickPanel(
    hazeState: HazeState,
    ruBypassEnabled: Boolean,
    perAppProxyEnabled: Boolean,
    onAction: (MainAction) -> Unit,
    onChooseApps: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassSurface(hazeState, PanelCardShape)
            .padding(vertical = 4.dp)
    ) {
        MainQuickSettings(
            ruBypassEnabled = ruBypassEnabled,
            perAppProxyEnabled = perAppProxyEnabled,
            onAction = onAction,
            onChooseApps = onChooseApps
        )
    }
}
