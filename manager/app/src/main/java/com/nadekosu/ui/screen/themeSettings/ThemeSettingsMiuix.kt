package com.nadekosu.ui.screen.themeSettings

import top.yukonga.miuix.kmp.utils.PressFeedbackType
import com.nadekosu.ui.screen.themeSettings.component.MiniPhonePreview
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.animation.animateColorAsState
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.DesignServices
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Pin
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.Swipe
import androidx.compose.material.icons.rounded.ViewCarousel
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.nadekosu.R
import com.nadekosu.data.HomeLayoutStyle
import com.nadekosu.data.UiStyle
import com.nadekosu.ksuApp
import com.nadekosu.ui.navigation.LocalNavigator
import com.nadekosu.ui.screen.themeSettings.component.ThemePreviewDialog
import com.nadekosu.ui.screen.themeSettings.component.ThemePreviewState
import com.nadekosu.ui.screen.themeSettings.component.ThemeSettingsCategory
import com.nadekosu.ui.theme.BackgroundManager
import com.nadekosu.ui.theme.CardConfig
import com.nadekosu.ui.theme.NadekoMiuixTheme
import com.nadekosu.ui.theme.ThemeConfig
import com.nadekosu.ui.viewmodel.HomeUiState
import com.nadekosu.ui.viewmodel.HomeViewModel
import com.nadekosu.ui.viewmodel.ModuleUiState
import com.nadekosu.ui.viewmodel.ModuleViewModel
import com.nadekosu.ui.viewmodel.SettingsUiState
import com.nadekosu.ui.viewmodel.SettingsViewModel
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardColors
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.TabRowDefaults
import top.yukonga.miuix.kmp.basic.TabRowWithContour
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.preference.ArrowPreference
import com.nadekosu.ui.component.miuix.ChoicePreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

/** Theme settings drawn with Miuix components. Same states and handlers as the Material page. */
@Composable
internal fun MiuixThemeSettingsScreen() {
    NadekoMiuixTheme { MiuixThemeSettingsContent() }
}

@Composable
private fun MiuixThemeSettingsContent() {
    val context = LocalContext.current
    val navigator = LocalNavigator.current

    val settingsViewModel = viewModel<SettingsViewModel>(viewModelStoreOwner = ksuApp)
    val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val homeViewModel = viewModel<HomeViewModel>(viewModelStoreOwner = ksuApp)
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val moduleViewModel = viewModel<ModuleViewModel>(viewModelStoreOwner = ksuApp)
    val moduleUiState by moduleViewModel.uiState.collectAsStateWithLifecycle()

    val scrollBehavior = MiuixScrollBehavior()
    val listState = rememberLazyListState()
    var selectedKey by rememberSaveable { mutableStateOf(ThemeSettingsCategory.COLORS.name) }
    val selected = ThemeSettingsCategory.entries.firstOrNull { it.name == selectedKey }
        ?: ThemeSettingsCategory.COLORS
    var showFullPreview by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(selected) { listState.scrollToItem(0) }

    val themePackage = rememberThemePackageActions(
        settingsState, homeUiState, settingsViewModel, homeViewModel
    )
    val pickBackground = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) settingsViewModel.handleCustomBackground(context, uri)
    }

    val hasBackground = ThemeConfig.customBackgroundUri != null
    val previewState = ThemePreviewState(
        dashboardLayout = homeUiState.homeLayoutStyle == HomeLayoutStyle.DASHBOARD,
        hasBanner = homeUiState.homeBannerFile != null,
        simpleMode = homeUiState.isSimpleMode,
        showHomeCardIcons = homeUiState.showHomeCardIcons,
        hideLinkCard = homeUiState.isHideLinkCard,
        hideOtherInfo = homeUiState.isHideOtherInfo,
        showNavBadge = homeUiState.showNavigationBarBadge,
        liquidGlass = ThemeConfig.isEnableLiquidGlass,
        topBarBlur = ThemeConfig.isEnableBlur,
        cardAlpha = CardConfig.cardAlpha,
        backgroundUri = ThemeConfig.customBackgroundUri,
        backgroundDim = ThemeConfig.backgroundDim,
    )
    val modeLabel = settingsState.themeOptions.getOrNull(settingsState.themeMode).orEmpty() +
            " · " + settingsState.dynamicPaletteStyle.displayName()

    ThemePreviewDialog(
        show = showFullPreview,
        previewState = previewState,
        onDismiss = { showFullPreview = false },
    )

    val surface = MiuixTheme.colorScheme.surface
    Scaffold(
        topBar = {
            TopAppBar(
                color = if (hasBackground) Color.Transparent else surface,
                title = stringResource(R.string.theme_settings),
                navigationIcon = {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.onBackground,
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        popupHost = { },
        containerColor = if (hasBackground) Color.Transparent else surface,
        contentWindowInsets = WindowInsets.systemBars
            .add(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Horizontal),
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp,
            ),
        ) {
            item {
                MiuixPreviewCard(
                    modeLabel = modeLabel,
                    previewState = previewState,
                    onClick = { showFullPreview = true },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
            item {
                MiuixCategoryGrid(selected = selected, onSelect = { selectedKey = it.name })
            }
            item(key = selected.name) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    when (selected) {
                        ThemeSettingsCategory.COLORS ->
                            MiuixColorsCategory(settingsState, settingsViewModel)

                        ThemeSettingsCategory.BACKGROUND -> MiuixBackgroundCategory(
                            settingsViewModel = settingsViewModel,
                            onPickBackground = { pickBackground.launch("image/*") },
                        )

                        ThemeSettingsCategory.ICON_TEXT ->
                            MiuixIconTextCategory(settingsState, settingsViewModel)

                        ThemeSettingsCategory.HOME_CARD ->
                            MiuixHomeCardCategory(homeUiState, homeViewModel)

                        ThemeSettingsCategory.TOP_BAR -> MiuixTopBarCategory()
                        ThemeSettingsCategory.BOTTOM_BAR ->
                            MiuixBottomBarCategory(homeUiState, homeViewModel)

                        ThemeSettingsCategory.GESTURES ->
                            MiuixGesturesCategory(settingsState, settingsViewModel)

                        ThemeSettingsCategory.DETAILS -> MiuixDetailsCategory(
                            homeUiState, homeViewModel, moduleUiState, moduleViewModel
                        )

                        ThemeSettingsCategory.THEME -> MiuixThemeCategory(themePackage)
                    }
                    MiuixMoreOptionsCard()
                }
            }
        }
    }
}

@Composable
private fun miuixCardColors(): CardColors {
    val base = MiuixTheme.colorScheme.surfaceContainer
    val color = if (ThemeConfig.customBackgroundUri != null) {
        base.copy(alpha = CardConfig.cardAlpha)
    } else {
        base
    }
    return CardDefaults.defaultColors(
        color = color,
        contentColor = MiuixTheme.colorScheme.onSurfaceContainer,
    )
}

@Composable
private fun SectionCard(
    category: ThemeSettingsCategory? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .padding(bottom = 12.dp)
            .fillMaxWidth(),
        colors = miuixCardColors(),
    ) {
        if (category != null) {
            BasicComponent(
                title = stringResource(category.labelRes),
                summary = stringResource(categorySummaryRes(category)),
                startAction = { PrefIcon(category.icon) },
            )
        }
        content()
    }
}

private fun categorySummaryRes(category: ThemeSettingsCategory): Int = when (category) {
    ThemeSettingsCategory.COLORS -> R.string.theme_category_colors_summary
    ThemeSettingsCategory.BACKGROUND -> R.string.theme_category_background_summary
    ThemeSettingsCategory.ICON_TEXT -> R.string.theme_category_icon_text_summary
    ThemeSettingsCategory.HOME_CARD -> R.string.theme_category_home_card_summary
    ThemeSettingsCategory.TOP_BAR -> R.string.theme_category_top_bar_summary
    ThemeSettingsCategory.BOTTOM_BAR -> R.string.theme_category_bottom_bar_summary
    ThemeSettingsCategory.GESTURES -> R.string.theme_category_gestures_summary
    ThemeSettingsCategory.DETAILS -> R.string.theme_category_details_summary
    ThemeSettingsCategory.THEME -> R.string.theme_category_theme_summary
}

@Composable
private fun PrefIcon(icon: ImageVector) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.padding(end = 6.dp),
        tint = MiuixTheme.colorScheme.onBackground,
    )
}

@Composable
private fun MiuixPreviewCard(
    modeLabel: String,
    previewState: ThemePreviewState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MiuixTheme.colorScheme
    Card(
        modifier = modifier.fillMaxWidth(),
        insideMargin = PaddingValues(0.dp),
        colors = miuixCardColors(),
        onClick = onClick,
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MiniPhonePreview(state = previewState, modifier = Modifier.width(88.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = stringResource(R.string.theme_preview_caption_current),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                )
                Text(
                    text = modeLabel,
                    fontSize = 12.sp,
                    color = colors.onSurfaceVariantSummary,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        colors.primary,
                        colors.secondaryContainer,
                        colors.tertiaryContainer,
                        colors.surfaceContainerHighest,
                    ).forEach { color ->
                        Spacer(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.theme_preview_compact_summary),
                    fontSize = 11.sp,
                    color = colors.onSurfaceVariantSummary,
                )
            }
        }
    }
}

@Composable
private fun MiuixCategoryGrid(
    selected: ThemeSettingsCategory,
    onSelect: (ThemeSettingsCategory) -> Unit,
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .padding(top = 4.dp)
            .fillMaxWidth(),
        insideMargin = PaddingValues(8.dp),
        colors = miuixCardColors(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ThemeSettingsCategory.entries.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    row.forEach { category ->
                        MiuixCategoryTile(
                            category = category,
                            selected = category == selected,
                            onClick = { onSelect(category) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiuixCategoryTile(
    category: ThemeSettingsCategory,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MiuixTheme.colorScheme
    val background by animateColorAsState(
        targetValue = if (selected) colors.primary else colors.surfaceContainerHighest,
        label = "ThemeCategoryBackground",
    )
    val contentColor = if (selected) colors.onPrimary else colors.onSurface
    val haptic = LocalHapticFeedback.current
    Column(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .selectable(
                selected = selected,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onClick()
                },
                role = Role.Tab,
            )
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = stringResource(category.labelRes),
            fontSize = 11.sp,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SliderPreference(
    title: String,
    icon: ImageVector,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChangeFinished: (Float) -> Unit,
) {
    var slider by remember(value) { mutableFloatStateOf(value) }
    BasicComponent(
        title = title,
        startAction = { PrefIcon(icon) },
        endActions = {
            Text(
                text = "${(slider * 100).toInt()}%",
                color = MiuixTheme.colorScheme.onSurfaceVariantActions,
            )
        },
        bottomAction = {
            Slider(
                value = slider,
                onValueChange = { slider = it },
                onValueChangeFinished = { onValueChangeFinished(slider) },
                valueRange = valueRange,
            )
        },
    )
}

@Composable
private fun MiuixColorsCategory(state: SettingsUiState, viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val styles = PaletteStyle.entries
    val specs = ColorSpec.SpecVersion.entries
    val colorNames = listOf(
        stringResource(R.string.miuix_color_default),
        stringResource(R.string.miuix_color_red),
        stringResource(R.string.miuix_color_pink),
        stringResource(R.string.miuix_color_purple),
        stringResource(R.string.miuix_color_deep_purple),
        stringResource(R.string.miuix_color_indigo),
        stringResource(R.string.miuix_color_blue),
        stringResource(R.string.miuix_color_cyan),
        stringResource(R.string.miuix_color_teal),
        stringResource(R.string.miuix_color_green),
        stringResource(R.string.miuix_color_yellow),
        stringResource(R.string.miuix_color_orange),
        stringResource(R.string.miuix_color_brown),
        stringResource(R.string.miuix_color_blue_grey),
    )
    SectionCard(ThemeSettingsCategory.COLORS) {
        TabRowWithContour(
            tabs = state.themeOptions,
            selectedTabIndex = state.themeMode.coerceIn(0, 2),
            onTabSelected = { index -> viewModel.handleThemeModeChange(context, index) },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            colors = TabRowDefaults.tabRowColors(
                backgroundColor = MiuixTheme.colorScheme.surfaceContainerHighest,
                selectedBackgroundColor = MiuixTheme.colorScheme.surfaceContainer,
            ),
        )
        HorizontalDivider()
        SwitchPreference(
            title = stringResource(R.string.miuix_enable_monet),
            summary = stringResource(R.string.miuix_enable_monet_summary),
            startAction = { PrefIcon(Icons.Rounded.Wallpaper) },
            checked = ThemeConfig.miuixMonet,
            onCheckedChange = { BackgroundManager.saveMiuixMonet(context, it) },
        )
        if (ThemeConfig.miuixMonet) {
            SwitchPreference(
                title = stringResource(R.string.dynamic_color_title),
                summary = stringResource(R.string.dynamic_color_summary),
                startAction = { PrefIcon(Icons.Rounded.Palette) },
                checked = state.useDynamicColor,
                onCheckedChange = { viewModel.handleDynamicColorChange(context, it) },
            )
            if (!state.useDynamicColor) {
                ChoicePreference(
                    title = stringResource(R.string.miuix_key_color),
                    items = colorNames,
                    startAction = { PrefIcon(Icons.Rounded.Colorize) },
                    selectedIndex = seedColorOptions.indexOf(ThemeConfig.seedColor)
                        .coerceAtLeast(0),
                    onSelectedIndexChange = { index ->
                        viewModel.handleThemeColorChange(context, seedColorOptions[index])
                    },
                )
            }
            ChoicePreference(
                title = stringResource(R.string.dynamic_palette_style),
                items = styles.map { it.displayName() },
                startAction = { PrefIcon(Icons.Rounded.Style) },
                selectedIndex = styles.indexOf(state.dynamicPaletteStyle).coerceAtLeast(0),
                onSelectedIndexChange = { index ->
                    viewModel.handleDynamicPaletteStyleChange(
                        context,
                        styles.getOrElse(index) { PaletteStyle.TonalSpot }
                    )
                },
            )
            ChoicePreference(
                title = stringResource(R.string.dynamic_color_spec),
                items = specs.map { it.displayName() },
                startAction = { PrefIcon(Icons.Rounded.DesignServices) },
                selectedIndex = specs.indexOf(state.dynamicColorSpec).coerceAtLeast(0),
                onSelectedIndexChange = { index ->
                    viewModel.handleDynamicColorSpecChange(
                        context,
                        specs.getOrElse(index) { ColorSpec.SpecVersion.SPEC_2021 }
                    )
                },
            )
        }
    }
}

@Composable
private fun MiuixBackgroundCategory(
    settingsViewModel: SettingsViewModel,
    onPickBackground: () -> Unit,
) {
    val context = LocalContext.current
    val hasBackground = ThemeConfig.customBackgroundUri != null
    SectionCard(ThemeSettingsCategory.BACKGROUND) {
        ArrowPreference(
            title = stringResource(R.string.settings_custom_background),
            summary = stringResource(R.string.settings_custom_background_summary),
            startAction = { PrefIcon(Icons.Rounded.Image) },
            onClick = onPickBackground,
        )
        if (hasBackground) {
            ArrowPreference(
                title = stringResource(R.string.theme_remove_background),
                startAction = { PrefIcon(Icons.Rounded.Delete) },
                onClick = { settingsViewModel.handleRemoveCustomBackground(context) },
            )
            SliderPreference(
                title = stringResource(R.string.settings_background_dim),
                icon = Icons.Rounded.Wallpaper,
                value = ThemeConfig.backgroundDim,
                valueRange = 0f..1f,
                onValueChangeFinished = { settingsViewModel.handleBackgroundDimChange(context, it) },
            )
            SliderPreference(
                title = stringResource(R.string.settings_card_alpha),
                icon = Icons.Rounded.Brush,
                value = CardConfig.cardAlpha,
                valueRange = 0.1f..1f,
                onValueChangeFinished = { settingsViewModel.handleCardAlphaChange(context, it) },
            )
            SwitchPreference(
                title = stringResource(R.string.settings_exp_draw_background_to_blur),
                summary = stringResource(R.string.settings_exp_draw_background_to_blur_description),
                startAction = { PrefIcon(Icons.Rounded.BlurOn) },
                checked = ThemeConfig.isEnableBlurExp,
                onCheckedChange = { BackgroundManager.saveEnableBlurExp(context, it) },
            )
            SwitchPreference(
                title = stringResource(R.string.settings_config_use_custom_background_seed_color),
                summary = stringResource(R.string.settings_config_use_custom_background_seed_color_summary),
                startAction = { PrefIcon(Icons.Rounded.Palette) },
                checked = ThemeConfig.isUseBackgroundSeedColor,
                onCheckedChange = { BackgroundManager.saveUseBackgroundSeedColor(context, it) },
            )
            SwitchPreference(
                title = stringResource(R.string.settings_custom_enable_high_contrast),
                summary = stringResource(R.string.settings_custom_enable_high_contrast_summary),
                startAction = { PrefIcon(Icons.Rounded.Contrast) },
                checked = ThemeConfig.isHighContrastMode,
                onCheckedChange = { BackgroundManager.saveEnableHighContrastMode(context, it) },
            )
        }
        SliderPreference(
            title = stringResource(R.string.settings_module_banner_alpha),
            icon = Icons.Rounded.Image,
            value = CardConfig.moduleBannerAlpha,
            valueRange = 0f..1f,
            onValueChangeFinished = { settingsViewModel.handleModuleBannerAlphaChange(context, it) },
        )
    }
}

@Composable
private fun MiuixIconTextCategory(state: SettingsUiState, viewModel: SettingsViewModel) {
    val context = LocalContext.current
    SectionCard(ThemeSettingsCategory.ICON_TEXT) {
        SwitchPreference(
            title = stringResource(R.string.icon_switch_title),
            summary = stringResource(R.string.icon_switch_summary),
            startAction = { PrefIcon(Icons.Rounded.Android) },
            checked = state.useAltIcon,
            onCheckedChange = { viewModel.handleIconChange(context, it) },
        )
        SwitchPreference(
            title = stringResource(R.string.settings_custom_monospace_font),
            summary = stringResource(R.string.settings_custom_monospace_font_summary),
            startAction = { PrefIcon(Icons.Rounded.FormatSize) },
            checked = state.useBuiltinMonoFont,
            onCheckedChange = { viewModel.handleBuiltinMonospaceFontChange(context, it) },
        )
    }
}

@Composable
private fun MiuixHomeCardCategory(homeUiState: HomeUiState, homeViewModel: HomeViewModel) {
    val context = LocalContext.current
    SectionCard(ThemeSettingsCategory.HOME_CARD) {
        ChoicePreference(
            title = stringResource(R.string.home_layout_style),
            items = listOf(
                stringResource(R.string.home_layout_circle),
                stringResource(R.string.home_layout_dashboard),
            ),
            startAction = { PrefIcon(Icons.Rounded.Dashboard) },
            selectedIndex = HomeLayoutStyle.supported.indexOf(homeUiState.homeLayoutStyle)
                .coerceAtLeast(0),
            onSelectedIndexChange = { index ->
                homeViewModel.handleHomeLayoutStyleChange(
                    context,
                    HomeLayoutStyle.supported.getOrElse(index) { HomeLayoutStyle.DEFAULT }
                )
            },
        )
        SwitchPreference(
            title = stringResource(R.string.simple_mode),
            summary = stringResource(R.string.simple_mode_summary),
            startAction = { PrefIcon(Icons.Rounded.Brush) },
            checked = homeUiState.isSimpleMode,
            onCheckedChange = { homeViewModel.handleSimpleModeChange(context, it) },
        )
        SwitchPreference(
            title = stringResource(R.string.home_card_icons),
            summary = stringResource(R.string.home_card_icons_summary),
            startAction = { PrefIcon(Icons.Rounded.Badge) },
            checked = homeUiState.showHomeCardIcons,
            onCheckedChange = { homeViewModel.handleHomeCardIconsChange(context, it) },
        )
    }
}

@Composable
private fun MiuixTopBarCategory() {
    val context = LocalContext.current
    SectionCard(ThemeSettingsCategory.TOP_BAR) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SwitchPreference(
                title = stringResource(R.string.settings_top_bar_blur),
                summary = stringResource(R.string.settings_top_bar_blur_summary),
                startAction = { PrefIcon(Icons.Rounded.BlurOn) },
                checked = ThemeConfig.isEnableBlur,
                onCheckedChange = { isChecked ->
                    BackgroundManager.saveEnableBlur(context, isChecked)
                    if (!isChecked && !ThemeConfig.isEnableLiquidGlass) {
                        BackgroundManager.saveEnableBlurExp(context, false)
                    }
                },
            )
        }
    }
}

@Composable
private fun MiuixBottomBarCategory(homeUiState: HomeUiState, homeViewModel: HomeViewModel) {
    val context = LocalContext.current
    SectionCard(ThemeSettingsCategory.BOTTOM_BAR) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SwitchPreference(
                title = stringResource(R.string.settings_bottom_bar_liquid_glass),
                summary = stringResource(R.string.settings_bottom_bar_liquid_glass_summary),
                startAction = { PrefIcon(Icons.Rounded.BlurOn) },
                checked = ThemeConfig.isEnableLiquidGlass,
                onCheckedChange = { isChecked ->
                    BackgroundManager.saveEnableLiquidGlass(context, isChecked)
                    if (!isChecked && !ThemeConfig.isEnableBlur) {
                        BackgroundManager.saveEnableBlurExp(context, false)
                    }
                },
            )
        }
        SwitchPreference(
            title = stringResource(R.string.navigation_bar_badge),
            summary = stringResource(R.string.navigation_bar_badge_summary),
            startAction = { PrefIcon(Icons.Rounded.Pin) },
            checked = homeUiState.showNavigationBarBadge,
            onCheckedChange = { homeViewModel.handleNavigationBarBadgeChange(context, it) },
        )
    }
}

@Composable
private fun MiuixGesturesCategory(state: SettingsUiState, viewModel: SettingsViewModel) {
    val context = LocalContext.current
    SectionCard(ThemeSettingsCategory.GESTURES) {
        SwitchPreference(
            title = stringResource(R.string.settings_enable_swipe_dismiss),
            summary = stringResource(R.string.settings_enable_swipe_dismiss_summary),
            startAction = { PrefIcon(Icons.Rounded.Swipe) },
            checked = state.enableSwipeDismiss,
            onCheckedChange = { viewModel.handleSwipeDismissChange(context, it) },
        )
        ChoicePreference(
            title = stringResource(R.string.settings_pager_gesture_mode),
            items = listOf(
                stringResource(R.string.settings_pager_gesture_native),
                stringResource(R.string.settings_pager_gesture_cross_axis),
                stringResource(R.string.settings_pager_gesture_ios_like),
            ),
            startAction = { PrefIcon(Icons.Rounded.ViewCarousel) },
            selectedIndex = state.pagerGestureMode.coerceIn(0, 2),
            onSelectedIndexChange = { viewModel.handlePagerGestureModeChange(context, it) },
        )
    }
}

@Composable
private fun MiuixDetailsCategory(
    homeUiState: HomeUiState,
    homeViewModel: HomeViewModel,
    moduleUiState: ModuleUiState,
    moduleViewModel: ModuleViewModel,
) {
    val context = LocalContext.current
    SectionCard(ThemeSettingsCategory.DETAILS) {
        SwitchPreference(
            title = stringResource(R.string.show_more_module_info),
            summary = stringResource(R.string.show_more_module_info_summary),
            startAction = { PrefIcon(Icons.Rounded.Info) },
            checked = moduleUiState.showMoreModuleInfo,
            onCheckedChange = { moduleViewModel.handleShowMoreModuleInfoChange(context, it) },
        )
        SwitchPreference(
            title = stringResource(R.string.hide_other_info),
            summary = stringResource(R.string.hide_other_info_summary),
            startAction = { PrefIcon(Icons.Rounded.VisibilityOff) },
            checked = homeUiState.isHideOtherInfo,
            onCheckedChange = homeViewModel::handleHideOtherInfoChange,
        )
        SwitchPreference(
            title = stringResource(R.string.hide_susfs_status),
            summary = stringResource(R.string.hide_susfs_status_summary),
            startAction = { PrefIcon(Icons.Rounded.VisibilityOff) },
            checked = homeUiState.isHideSusfsStatus,
            onCheckedChange = homeViewModel::handleHideSusfsStatusChange,
        )
        SwitchPreference(
            title = stringResource(R.string.hide_zygisk_implement),
            summary = stringResource(R.string.hide_zygisk_implement_summary),
            startAction = { PrefIcon(Icons.Rounded.VisibilityOff) },
            checked = homeUiState.isHideZygiskImplement,
            onCheckedChange = homeViewModel::handleHideZygiskImplementChange,
        )
        SwitchPreference(
            title = stringResource(R.string.hide_meta_module_implement),
            summary = stringResource(R.string.hide_meta_module_implement_summary),
            startAction = { PrefIcon(Icons.Rounded.VisibilityOff) },
            checked = homeUiState.isHideMetaModuleImplement,
            onCheckedChange = homeViewModel::handleHideMetaModuleImplementChange,
        )
        SwitchPreference(
            title = stringResource(R.string.hide_link_card),
            summary = stringResource(R.string.hide_link_card_summary),
            startAction = { PrefIcon(Icons.Rounded.VisibilityOff) },
            checked = homeUiState.isHideLinkCard,
            onCheckedChange = homeViewModel::handleHideLinkCardChange,
        )
        SwitchPreference(
            title = stringResource(R.string.hide_tag_card),
            summary = stringResource(R.string.hide_tag_card_summary),
            startAction = { PrefIcon(Icons.Rounded.VisibilityOff) },
            checked = moduleUiState.isHideTagRow,
            onCheckedChange = moduleViewModel::handleHideTagRowChange,
        )
    }
}

@Composable
private fun MiuixThemeCategory(actions: ThemePackageActions) {
    val context = LocalContext.current
    SectionCard(ThemeSettingsCategory.THEME) {
        ChoicePreference(
            title = stringResource(R.string.ui_style_title),
            items = listOf(
                stringResource(R.string.ui_style_material),
                stringResource(R.string.ui_style_miuix),
            ),
            startAction = { PrefIcon(Icons.Rounded.Style) },
            selectedIndex = UiStyle.supported.indexOf(ThemeConfig.uiStyle).coerceAtLeast(0),
            onSelectedIndexChange = { index ->
                BackgroundManager.saveUiStyle(
                    context,
                    UiStyle.supported.getOrElse(index) { UiStyle.DEFAULT }
                )
            },
        )
        ArrowPreference(
            title = stringResource(R.string.theme_import),
            summary = stringResource(R.string.theme_import_summary),
            startAction = { PrefIcon(Icons.Rounded.FileDownload) },
            onClick = actions.importTheme,
        )
        ArrowPreference(
            title = stringResource(R.string.theme_export),
            summary = stringResource(R.string.theme_export_summary),
            startAction = { PrefIcon(Icons.Rounded.FileUpload) },
            onClick = actions.exportTheme,
        )
        ArrowPreference(
            title = stringResource(R.string.theme_reset),
            summary = stringResource(R.string.theme_reset_summary),
            startAction = { PrefIcon(Icons.Rounded.Restore) },
            onClick = actions.resetTheme,
        )
    }
}

/** Options that only exist in the Material page for now. */
@Composable
private fun MiuixMoreOptionsCard() {
    val context = LocalContext.current
    SectionCard {
        ArrowPreference(
            title = stringResource(R.string.miuix_more_options),
            summary = stringResource(R.string.miuix_more_options_summary),
            startAction = { PrefIcon(Icons.Rounded.Style) },
            onClick = { BackgroundManager.saveUiStyle(context, UiStyle.MATERIAL) },
        )
    }
}
