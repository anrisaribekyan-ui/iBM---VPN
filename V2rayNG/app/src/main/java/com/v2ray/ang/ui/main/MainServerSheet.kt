package com.v2ray.ang.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.v2ray.ang.R
import com.v2ray.ang.dto.GroupMapItem
import com.v2ray.ang.dto.LocateTarget
import com.v2ray.ang.dto.entities.ProfileItem
import com.v2ray.ang.ui.compose.AmbientBackground
import com.v2ray.ang.ui.compose.glassBackdrop
import com.v2ray.ang.ui.compose.glassSurface
import com.v2ray.ang.ui.compose.verticalScrollbar
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

private val SheetBarShape = RoundedCornerShape(24.dp)

/**
 * Full server list in a modal bottom sheet: group tabs, search, the server pager and the
 * server-management menus (import, test, sort, delete...) that no longer sit on the main screen.
 * Selecting a server selects it and closes the sheet.
 */
// ModalBottomSheet is still @ExperimentalMaterial3Api in material3 and has no stable
// equivalent for a modal sheet. Re-evaluate and drop the opt-in once it graduates to stable.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainServerSheet(
    mainViewModel: MainViewModel,
    groups: List<GroupMapItem>,
    selectedGroupId: String,
    selectedGuid: String?,
    locateTarget: LocateTarget?,
    doubleColumnDisplay: Boolean,
    isRunning: Boolean,
    lazyListStates: MutableMap<String, LazyListState>,
    lazyGridStates: MutableMap<String, LazyGridState>,
    onAction: (MainAction) -> Unit,
    onMoreMenuAction: (MainMoreMenuAction) -> Unit,
    onShareServer: (String, ProfileItem) -> Unit,
    onMoreServer: (String, ProfileItem) -> Unit,
    onRemoveServer: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    // iBM: a hard fling toward the top of the list used to hand its leftover velocity to the
    // sheet, which dragged down and snapped back (visible shaking). Leftover fling scroll and
    // velocity stop here; a finger drag still reaches the sheet, so swipe-to-close keeps working.
    val listFlingStopsAtSheet = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset =
                if (source == NestedScrollSource.SideEffect) Offset(0f, available.y) else Offset.Zero

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity =
                Velocity(0f, available.y)
        }
    }
    val scope = rememberCoroutineScope()
    val hazeState = rememberHazeState()
    val density = LocalDensity.current
    var topChromeHeight by remember { mutableStateOf(0.dp) }
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showImportMenu by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    val importMenuScrollState = rememberScrollState()
    val moreMenuScrollState = rememberScrollState()
    val maxMenuHeight = LocalConfiguration.current.screenHeightDp.dp * 0.7f

    val latestOnAction by rememberUpdatedState(onAction)
    // The keyword filter lives in the ViewModel; never leave it applied after the sheet is gone.
    DisposableEffect(Unit) {
        onDispose { latestOnAction(MainAction.Search("")) }
    }

    // Back closes the search field first; only a second Back dismisses the sheet.
    BackHandler(enabled = showSearch) {
        searchQuery = ""
        onAction(MainAction.Search(""))
        showSearch = false
    }

    val closeSheet: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    val pagerState = rememberPagerState(
        initialPage = groups.indexOfFirst { it.id == selectedGroupId }.coerceAtLeast(0),
        pageCount = { groups.size.coerceAtLeast(1) }
    )

    LaunchedEffect(groups, selectedGroupId) {
        if (groups.isEmpty()) return@LaunchedEffect
        val selectedIndex = groups.indexOfFirst { it.id == selectedGroupId }
            .takeIf { it >= 0 } ?: 0
        if (!pagerState.isScrollInProgress && pagerState.settledPage != selectedIndex) {
            pagerState.scrollToPage(selectedIndex)
        }
    }

    val latestGroups by rememberUpdatedState(groups)

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                val currentGroups = latestGroups
                if (page in currentGroups.indices) {
                    onAction(MainAction.SelectGroup(currentGroups[page].id))
                }
            }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {
            AmbientBackground(
                active = isRunning,
                modifier = Modifier.glassBackdrop(hazeState)
            ) {
                if (groups.isNotEmpty()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(listFlingStopsAtSheet),
                        userScrollEnabled = true,
                        beyondViewportPageCount = 1,
                        key = { page -> groups.getOrNull(page)?.id ?: "group-page-$page" }
                    ) { page ->
                        val group = groups.getOrNull(page) ?: return@HorizontalPager

                        GroupPagerPage(
                            groupId = group.id,
                            mainViewModel = mainViewModel,
                            selectedGuid = selectedGuid,
                            locateTarget = locateTarget,
                            doubleColumnDisplay = doubleColumnDisplay,
                            searchQuery = searchQuery,
                            lazyListStates = lazyListStates,
                            lazyGridStates = lazyGridStates,
                            onSelectServer = { guid ->
                                onAction(MainAction.SelectServer(guid))
                                closeSheet()
                            },
                            onEditServer = { guid, profile -> onAction(MainAction.EditServer(guid, profile)) },
                            onShareServer = onShareServer,
                            onMoreServer = onMoreServer,
                            onRemoveServer = onRemoveServer,
                            contentPadding = PaddingValues(
                                start = 12.dp,
                                top = topChromeHeight + 10.dp,
                                end = 12.dp,
                                bottom = 16.dp
                            )
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = topChromeHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.empty_servers_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .onSizeChanged { topChromeHeight = with(density) { it.height.toDp() } }
                    .padding(start = 12.dp, end = 12.dp, top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .glassSurface(hazeState, SheetBarShape)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showSearch) {
                        IconButton(
                            onClick = {
                                searchQuery = ""
                                onAction(MainAction.Search(""))
                                showSearch = false
                            }
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_arrow_back_24dp),
                                contentDescription = stringResource(R.string.acc_back),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        SheetSearchField(
                            query = searchQuery,
                            onQueryChange = { query ->
                                searchQuery = query
                                onAction(MainAction.Search(query))
                            },
                            placeholder = stringResource(R.string.menu_item_search),
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.ibm_servers),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 16.dp)
                        )
                        IconButton(onClick = { showSearch = true }) {
                            Icon(
                                painterResource(R.drawable.ic_search_24dp),
                                contentDescription = stringResource(R.string.acc_search),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                        IconButton(onClick = { showImportMenu = true }) {
                            Icon(
                                painterResource(R.drawable.ic_add_24dp),
                                contentDescription = stringResource(R.string.acc_add),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        DropdownMenu(
                            expanded = showImportMenu,
                            onDismissRequest = { showImportMenu = false },
                            scrollState = importMenuScrollState,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .heightIn(max = maxMenuHeight)
                                .verticalScrollbar(importMenuScrollState)
                        ) {
                            ImportMenuContent(
                                onAction = { action ->
                                    showImportMenu = false
                                    onAction(action)
                                }
                            )
                        }
                    }
                    Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(
                                painterResource(R.drawable.ic_more_vert_24dp),
                                contentDescription = stringResource(R.string.acc_more),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false },
                            scrollState = moreMenuScrollState,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .heightIn(max = maxMenuHeight)
                                .verticalScrollbar(moreMenuScrollState)
                        ) {
                            MoreMenuContent { action ->
                                showMoreMenu = false
                                onMoreMenuAction(action)
                            }
                        }
                    }
                }
                if (groups.size > 1) {
                    GroupTabBar(
                        hazeState = hazeState,
                        groups = groups,
                        selectedTabIndex = pagerState.currentPage.coerceIn(0, groups.lastIndex),
                        mainViewModel = mainViewModel,
                        onTabClick = { targetIndex ->
                            scope.launch {
                                pagerState.navigateToPageOptimized(
                                    targetPage = targetIndex,
                                    animateAdjacentPage = true
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SheetSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    val textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
    Box(modifier = modifier.padding(horizontal = 4.dp), contentAlignment = Alignment.CenterStart) {
        if (query.isEmpty()) {
            Text(
                text = placeholder,
                style = textStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = textStyle,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )
    }
}
