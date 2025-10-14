package com.rosan.installer.ui.page.main.settings.main

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FlexibleBottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rosan.installer.R
import com.rosan.installer.ui.icons.AppIcons
import com.rosan.installer.ui.page.main.settings.config.all.AllPage
import com.rosan.installer.ui.page.main.settings.config.all.AllViewAction
import com.rosan.installer.ui.page.main.settings.config.all.AllViewModel
import com.rosan.installer.ui.page.main.settings.config.all.NewAllPage
import com.rosan.installer.ui.page.main.settings.preferred.NewPreferredPage
import com.rosan.installer.ui.page.main.settings.preferred.PreferredPage
import com.rosan.installer.ui.page.main.settings.preferred.PreferredViewModel
import com.rosan.installer.ui.theme.exclude
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(DelicateCoroutinesApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainPage(navController: NavController, preferredViewModel: PreferredViewModel) {
    val allViewModel: AllViewModel = koinViewModel {
        parametersOf(navController)
    }
    LaunchedEffect(Unit) {
        allViewModel.dispatch(AllViewAction.Init)
    }
    val configCount = allViewModel.state.data.configs.size
    val data = arrayOf(
        NavigationData(
            icon = AppIcons.RoomPreferences,
            label = stringResource(R.string.config)
        ) { insets ->
            if (preferredViewModel.state.showExpressiveUI)
                NewAllPage(navController, insets, allViewModel)
            else
                AllPage(navController, insets, allViewModel)
        },
        NavigationData(
            icon = AppIcons.SettingsSuggest,
            label = stringResource(R.string.preferred)
        ) { insets ->
            if (preferredViewModel.state.showExpressiveUI)
                NewPreferredPage(navController, preferredViewModel)
            else
                PreferredPage(navController, insets, preferredViewModel)
        }
    )
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { data.size })
    val currentPage = pagerState.currentPage
    fun onPageChanged(page: Int) {
        scope.launch { // 使用 rememberCoroutineScope更安全
            pagerState.animateScrollToPage(page = page)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val isLandscapeScreen = this.maxHeight.value / this.maxWidth.value > 1.4

        val navigationSide =
            if (isLandscapeScreen) WindowInsetsSides.Bottom
            else WindowInsetsSides.Left

        val navigationWindowInsets = WindowInsets.safeDrawing.only(
            (if (isLandscapeScreen) WindowInsetsSides.Horizontal
            else WindowInsetsSides.Vertical) + navigationSide
        )
        val pageWindowInsets = WindowInsets.safeDrawing.exclude(navigationSide)

        Row(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (!isLandscapeScreen) {
                ColumnNavigation(
                    windowInsets = navigationWindowInsets,
                    data = data,
                    currentPage = currentPage,
                    onPageChanged = { onPageChanged(it) }
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = true,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) { page ->
                    data[page].content.invoke(pageWindowInsets)
                }
                if (isLandscapeScreen) {
                    RowNavigation(
                        windowInsets = navigationWindowInsets,
                        data = data,
                        currentPage = currentPage,
                        onPageChanged = { onPageChanged(it) },
                        configCount
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RowNavigation(
    windowInsets: WindowInsets,
    data: Array<NavigationData>,
    currentPage: Int,
    onPageChanged: (Int) -> Unit,
    configCount: Int
) {
    FlexibleBottomAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(),
        windowInsets = windowInsets,
        expandedHeight = 72.dp,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        horizontalArrangement = BottomAppBarDefaults.FlexibleFixedHorizontalArrangement,
        content = {
            data.forEachIndexed { index, navigationData ->
                NavigationBarItem(
                    selected = currentPage == index,
                    onClick = { onPageChanged(index) },
                    icon = {
                        val showBadge = index == 0 && configCount > 1

                        BadgedBox(
                            badge = {
                                androidx.compose.animation.AnimatedVisibility(
                                    visible = showBadge,
                                    enter = scaleIn() + fadeIn(),
                                    exit = scaleOut() + fadeOut(),
                                    label = "badge"
                                ) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        contentColor = MaterialTheme.colorScheme.onSecondary
                                    ) { Text(configCount.toString()) }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = navigationData.icon,
                                contentDescription = navigationData.label
                            )
                        }
                    },
                    label = {
                        Text(text = navigationData.label)
                    },
                    alwaysShowLabel = false
                )
            }
        }
    )

    /*NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(),
        windowInsets = windowInsets
    ) {
        data.forEachIndexed { index, navigationData ->
            NavigationBarItem(
                selected = currentPage == index,
                onClick = { onPageChanged(index) },
                icon = {
                    Icon(
                        imageVector = navigationData.icon,
                        contentDescription = navigationData.label
                    )
                },
                label = {
                    Text(text = navigationData.label)
                },
                alwaysShowLabel = false
            )
        }
    }*/
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ColumnNavigation(
    windowInsets: WindowInsets,
    data: Array<NavigationData>,
    currentPage: Int,
    onPageChanged: (Int) -> Unit
) {
    val state = rememberWideNavigationRailState()
    val scope = rememberCoroutineScope()

    WideNavigationRail(
        state = state,
        windowInsets = windowInsets,
        //expandedHeaderTopPadding = 64.dp,
        header = {
            IconButton(
                modifier =
                    Modifier
                        .padding(start = 24.dp)
                        .semantics {
                            // The button must announce the expanded or collapsed state of the rail
                            // for accessibility.
                            stateDescription =
                                if (state.currentValue == WideNavigationRailValue.Expanded) {
                                    "Expanded"
                                } else {
                                    "Collapsed"
                                }
                        },
                onClick = {
                    scope.launch {
                        if (state.targetValue == WideNavigationRailValue.Expanded)
                            state.collapse()
                        else state.expand()
                    }
                },
            ) {
                if (state.targetValue == WideNavigationRailValue.Expanded) {
                    Icon(AppIcons.MenuOpen, "Collapse rail")
                } else {
                    Icon(AppIcons.Menu, "Expand rail")
                }
            }
        }
    ) {
        data.forEachIndexed { index, navigationData ->
            WideNavigationRailItem(
                railExpanded = state.targetValue == WideNavigationRailValue.Expanded,
                selected = currentPage == index,
                onClick = { onPageChanged(index) },
                icon = {
                    Icon(
                        imageVector = navigationData.icon,
                        contentDescription = navigationData.label
                    )
                },
                label = {
                    Text(text = navigationData.label)
                }
            )
        }
    }
    /*NavigationRail(
        modifier = Modifier
            .fillMaxHeight()
            .wrapContentSize(),
        windowInsets = windowInsets
    ) {
        Spacer(
            modifier = Modifier
                .weight(1f)
        )
        data.forEachIndexed { index, navigationData ->
            NavigationRailItem(
                selected = currentPage == index,
                onClick = { onPageChanged(index) },
                icon = {
                    Icon(
                        imageVector = navigationData.icon,
                        contentDescription = navigationData.label
                    )
                },
                label = {
                    Text(text = navigationData.label)
                },
                alwaysShowLabel = false
            )
        }
    }*/
}
