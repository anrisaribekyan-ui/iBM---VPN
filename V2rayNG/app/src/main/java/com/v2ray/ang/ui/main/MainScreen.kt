package com.v2ray.ang.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.v2ray.ang.R
import com.v2ray.ang.dto.entities.ProfileItem
import com.v2ray.ang.ui.compose.AmbientBackground
import com.v2ray.ang.ui.compose.QRCodeDialog
import com.v2ray.ang.ui.compose.glassBackdrop
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

/**
 * Minimal main screen: title + menu button, the connect button with a status line, the selected
 * server card and two quick toggles. The full server list lives in [MainServerSheet]; every other
 * feature is reachable from the drawer.
 */
@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    onAction: (MainAction) -> Unit,
    onNavigate: (MainDestination) -> Unit,
) {
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val groups = uiState.groups
    val isLoading by mainViewModel.isLoading.collectAsStateWithLifecycle()
    val isRunning = uiState.isRunning
    val displayText = mainViewModel.formatStatus(uiState.status)
    val selectedGuid = uiState.selectedGuid
    val confirmRemove = uiState.confirmRemove
    val shareQRCodeBitmap = uiState.shareQRCodeBitmap

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showServerSheet by rememberSaveable { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showDelAllConfirm by remember { mutableStateOf(false) }
    var showDelDuplicateConfirm by remember { mutableStateOf(false) }
    var showDelInvalidConfirm by remember { mutableStateOf(false) }
    var showRemoveConfirm by rememberSaveable(stateSaver = ServerDeleteTarget.Saver) {
        mutableStateOf<ServerDeleteTarget?>(null)
    }

    var shareTarget by remember { mutableStateOf<Triple<String, ProfileItem, Boolean>?>(null) }
    val removeServer: (String, String) -> Unit = { guid, profileName ->
        if (confirmRemove) {
            showRemoveConfirm = ServerDeleteTarget(guid, profileName)
        } else {
            onAction(MainAction.RemoveServer(guid))
        }
    }

    val lazyListStates = remember { mutableStateMapOf<String, LazyListState>() }
    val lazyGridStates = remember { mutableStateMapOf<String, LazyGridState>() }

    LaunchedEffect(groups) {
        val validGroupIds = groups.map { it.id }.toSet()
        lazyListStates.keys.retainAll(validGroupIds)
        lazyGridStates.keys.retainAll(validGroupIds)
    }

    // Latency of the selected server comes from the group list the user last browsed.
    val serverFlows = remember(groups, mainViewModel) {
        groups.map { mainViewModel.serversForGroup(it.id) }
    }
    // Look the selected server up in every group, not only the one last browsed.
    val selectedDelayMillis by remember(serverFlows, selectedGuid) {
        if (serverFlows.isEmpty() || selectedGuid == null) {
            flowOf(0L)
        } else {
            combine(serverFlows) { lists ->
                lists.asSequence()
                    .flatMap { it.asSequence() }
                    .firstOrNull { it.guid == selectedGuid }
                    ?.testDelayMillis ?: 0L
            }
        }
    }.collectAsStateWithLifecycle(initialValue = 0L)
    val hasServers by remember(serverFlows) {
        if (serverFlows.isEmpty()) {
            flowOf(false)
        } else {
            combine(serverFlows) { lists -> lists.any { it.isNotEmpty() } }
        }
    }.collectAsStateWithLifecycle(initialValue = false)
    val showEmptyState = !isLoading && !hasServers && selectedGuid.isNullOrEmpty()

    MainDialogs(
        showDelAllConfirm = showDelAllConfirm,
        onDismissDelAll = { showDelAllConfirm = false },
        onConfirmDelAll = { showDelAllConfirm = false; onAction(MainAction.RemoveAllServers) },
        showDelDuplicateConfirm = showDelDuplicateConfirm,
        onDismissDelDuplicate = { showDelDuplicateConfirm = false },
        onConfirmDelDuplicate = { showDelDuplicateConfirm = false; onAction(MainAction.RemoveDuplicateServers) },
        showDelInvalidConfirm = showDelInvalidConfirm,
        onDismissDelInvalid = { showDelInvalidConfirm = false },
        onConfirmDelInvalid = { showDelInvalidConfirm = false; onAction(MainAction.RemoveInvalidServers) },
        showRemoveConfirm = showRemoveConfirm,
        onDismissRemove = { showRemoveConfirm = null },
        onConfirmRemove = { guid -> showRemoveConfirm = null; onAction(MainAction.RemoveServer(guid)) }
    )

    if (shareTarget != null) {
        val (guid, profile, more) = shareTarget!!
        ShareMethodDialog(
            guid = guid,
            profile = profile,
            more = more,
            onDismiss = { shareTarget = null },
            onAction = onAction,
            onRemove = removeServer,
        )
    }
    if (shareQRCodeBitmap != null) {
        QRCodeDialog(bitmap = shareQRCodeBitmap, onDismiss = { onAction(MainAction.DismissQRCodeDialog) })
    }
    if (showImportDialog) {
        ImportMethodDialog(
            onDismiss = { showImportDialog = false },
            onAction = onAction
        )
    }

    val onMoreMenuAction: (MainMoreMenuAction) -> Unit = { action ->
        when (action) {
            MainMoreMenuAction.RestartService -> onAction(MainAction.RestartService)
            MainMoreMenuAction.DeleteAll -> showDelAllConfirm = true
            MainMoreMenuAction.DeleteDuplicate -> showDelDuplicateConfirm = true
            MainMoreMenuAction.DeleteInvalid -> showDelInvalidConfirm = true
            MainMoreMenuAction.ExportAll -> onAction(MainAction.ExportAll)
            MainMoreMenuAction.LocateSelected -> onAction(MainAction.LocateSelectedServer)
            MainMoreMenuAction.SortByTestResults -> onAction(MainAction.SortByTestResults)
            MainMoreMenuAction.TestAll -> onAction(MainAction.TestAllServers)
            MainMoreMenuAction.TestAllRealPing -> onAction(MainAction.TestRealAllServers)
            MainMoreMenuAction.UpdateSubscriptions -> onAction(MainAction.UpdateSubscriptions)
        }
    }

    if (showServerSheet) {
        MainServerSheet(
            mainViewModel = mainViewModel,
            groups = groups,
            selectedGroupId = uiState.selectedGroupId,
            selectedGuid = selectedGuid,
            locateTarget = uiState.locateTarget,
            doubleColumnDisplay = uiState.doubleColumnDisplay,
            isRunning = isRunning,
            lazyListStates = lazyListStates,
            lazyGridStates = lazyGridStates,
            onAction = onAction,
            onMoreMenuAction = onMoreMenuAction,
            onShareServer = { guid, profile -> shareTarget = Triple(guid, profile, false) },
            onMoreServer = { guid, profile -> shareTarget = Triple(guid, profile, true) },
            onRemoveServer = removeServer,
            onDismiss = { showServerSheet = false }
        )
    }

    val hazeState = rememberHazeState()

    val statusTitle = stringResource(
        if (isRunning) R.string.status_connected_title else R.string.ibm_status_disconnected
    )
    val statusDetail = when (uiState.status) {
        MainStatus.Connected, MainStatus.Disconnected -> null
        else -> displayText
    }
    val statusHint = if (statusDetail == null && isRunning) {
        stringResource(R.string.status_tap_to_test)
    } else null

    ModalNavigationDrawer(
        drawerState = drawerState,
        scrimColor = Color.Black.copy(alpha = 0.55f),
        drawerContent = {
            MainDrawerContent(
                drawerState = drawerState,
                onAction = { item ->
                    scope.launch { drawerState.close() }
                    when (item) {
                        MainDrawerAction.UpdateSubscriptions -> onAction(MainAction.UpdateAllSubscriptions)
                        MainDrawerAction.ImportClipboard -> onAction(MainAction.ImportClipboardToDefaultGroup)
                        MainDrawerAction.Import -> showImportDialog = true
                        MainDrawerAction.Servers -> showServerSheet = true
                    }
                },
                onNavigate = { route ->
                    scope.launch { drawerState.close() }
                    onNavigate(route)
                }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Everything drawn here is the backdrop the glass cards refract.
            AmbientBackground(
                active = isRunning,
                modifier = Modifier.glassBackdrop(hazeState)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                MainTopBar(
                    isLoading = isLoading,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                if (showEmptyState) {
                    MainEmptyState(
                        onAddSubscription = { onAction(MainAction.ImportClipboardToDefaultGroup) },
                        onOtherImport = { showImportDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        MainConnectSection(
                            isRunning = isRunning,
                            statusTitle = statusTitle,
                            statusDetail = statusDetail,
                            statusHint = statusHint,
                            onToggle = { onAction(MainAction.ToggleService) },
                            onTest = { onAction(MainAction.TestCurrentServer) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MainServerCard(
                            hazeState = hazeState,
                            serverName = uiState.selectedServerName,
                            delayMillis = selectedDelayMillis,
                            onClick = { showServerSheet = true }
                        )
                        MainQuickPanel(
                            hazeState = hazeState,
                            ruBypassEnabled = uiState.ruBypassEnabled,
                            perAppProxyEnabled = uiState.perAppProxyEnabled,
                            onAction = onAction,
                            onChooseApps = { onNavigate(MainDestination.PerAppProxy) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MainEmptyState(
    onAddSubscription: () -> Unit,
    onOtherImport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_shield_24dp),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.empty_servers_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.ibm_empty_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(28.dp))
        Button(onClick = onAddSubscription) {
            Text(stringResource(R.string.ibm_add_subscription))
        }
        Spacer(Modifier.height(4.dp))
        TextButton(onClick = onOtherImport) {
            Text(stringResource(R.string.ibm_other_import_methods))
        }
    }
}
