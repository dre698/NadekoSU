package com.nadekosu.ui.screen.themeSettings

import com.nadekosu.ui.theme.ThemeSeedColors
import com.nadekosu.ui.screen.themeSettings.component.ThemeSettingsCategory
import com.nadekosu.ui.screen.themeSettings.component.ThemePreviewDialog
import com.nadekosu.ui.screen.themeSettings.component.ThemePreviewCard
import com.nadekosu.ui.screen.themeSettings.component.ThemeCategoryGrid
import com.materialkolor.dynamicColorScheme
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Shape
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material.icons.twotone.BrightnessAuto
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Android
import androidx.compose.material.icons.twotone.Animation
import androidx.compose.material.icons.twotone.Badge
import androidx.compose.material.icons.twotone.BlurOn
import androidx.compose.material.icons.twotone.Brush
import androidx.compose.material.icons.twotone.Check
import androidx.compose.material.icons.twotone.ColorLens
import androidx.compose.material.icons.twotone.Contrast
import androidx.compose.material.icons.twotone.Dashboard
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material.icons.twotone.Image
import androidx.compose.material.icons.twotone.DarkMode
import androidx.compose.material.icons.twotone.DesignServices
import androidx.compose.material.icons.twotone.Dock
import androidx.compose.material.icons.twotone.Draw
import androidx.compose.material.icons.twotone.FormatColorFill
import androidx.compose.material.icons.twotone.FormatSize
import androidx.compose.material.icons.twotone.Info
import androidx.compose.material.icons.twotone.Label
import androidx.compose.material.icons.twotone.LightMode
import androidx.compose.material.icons.twotone.Opacity
import androidx.compose.material.icons.twotone.Palette
import androidx.compose.material.icons.twotone.Pin
import androidx.compose.material.icons.twotone.Style
import androidx.compose.material.icons.twotone.SwapHoriz
import androidx.compose.material.icons.twotone.Swipe
import androidx.compose.material.icons.twotone.Translate
import androidx.compose.material.icons.twotone.ViewCarousel
import androidx.compose.material.icons.twotone.VisibilityOff
import androidx.compose.material.icons.twotone.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.nadekosu.R
import com.nadekosu.data.HomeLayoutStyle
import com.nadekosu.ksuApp
import com.nadekosu.ui.util.HomeBannerStorage
import com.nadekosu.ui.component.ConfirmResult
import com.nadekosu.ui.component.KeyPointSlider
import com.nadekosu.ui.component.rememberConfirmDialog
import com.nadekosu.ui.component.settings.AppBackButton
import com.nadekosu.ui.component.settings.SegmentedColumn
import com.nadekosu.ui.component.settings.SegmentedColumnScope
import com.nadekosu.ui.component.settings.SettingsBaseWidget
import com.nadekosu.ui.component.settings.SettingsChooseDialog
import com.nadekosu.ui.component.settings.SettingsChooseWidget
import com.nadekosu.ui.component.settings.SettingsJumpPageWidget
import com.nadekosu.ui.component.settings.SettingsSwitchWidget
import com.nadekosu.ui.navigation.LocalNavigator
import com.nadekosu.ui.screen.themeSettings.component.LanguageSelectionDialog
import com.nadekosu.ui.screen.themeSettings.component.ThemeSettingsDialogs
import com.nadekosu.ui.screen.themeSettings.crop.BackgroundCropActivity
import com.nadekosu.ui.screen.themeSettings.util.restartActivity
import com.nadekosu.ui.theme.BackgroundManager
import com.nadekosu.ui.theme.CardConfig
import com.nadekosu.ui.theme.ThemeConfig
import com.nadekosu.ui.theme.blurEffect
import com.nadekosu.ui.theme.blurSource
import com.nadekosu.ui.theme.renderBackgroundBlur
import com.nadekosu.ui.viewmodel.HomeUiState
import com.nadekosu.ui.viewmodel.HomeViewModel
import com.nadekosu.ui.viewmodel.ModuleUiState
import com.nadekosu.ui.viewmodel.ModuleViewModel
import com.nadekosu.ui.viewmodel.PredictiveBackAnimation
import com.nadekosu.ui.viewmodel.PredictiveBackExitDirection
import com.nadekosu.ui.viewmodel.SettingsUiState
import com.nadekosu.ui.viewmodel.SettingsViewModel
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.roundToInt

@SuppressLint(
    "LocalContextConfigurationRead", "LocalContextResourcesRead", "ObsoleteSdkInt",
    "RestrictedApi"
)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ThemeSettingsScreen() {
    // 顶部滚动行为
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val systemIsDark = isSystemInDarkTheme()

    // 创建设置状态管理器
    val settingsViewModel = viewModel<SettingsViewModel>(viewModelStoreOwner = ksuApp)
    val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    val homeViewModel = viewModel<HomeViewModel>(viewModelStoreOwner = ksuApp)
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    val moduleViewModel = viewModel<ModuleViewModel>(viewModelStoreOwner = ksuApp)
    val moduleUiState by moduleViewModel.uiState.collectAsStateWithLifecycle()

    // Image selection and cropping
    var pendingBackgroundImageUri by remember { mutableStateOf<Uri?>(null) }
    var pendingExternalCropOutputUri by remember { mutableStateOf<Uri?>(null) }
    var showCropMethodDialog by remember { mutableStateOf(false) }

    val externalCropUnavailable = stringResource(R.string.background_external_crop_unavailable)
    val backgroundCropFailed = stringResource(R.string.background_crop_failed)

    val uCropLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { data ->
                UCrop.getOutput(data)?.let {
                    settingsViewModel.handleCustomBackground(context, it)
                }
            }
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            Toast.makeText(
                context,
                backgroundCropFailed,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val externalCropLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val outputUri = pendingExternalCropOutputUri ?: result.data?.data
            outputUri?.let {
                settingsViewModel.handleCustomBackground(context, it)
            }
        }
        pendingExternalCropOutputUri = null
    }

    val launchInAppCrop = { sourceUri: Uri ->
        val outputUri = createBackgroundCropOutputUri(context, "background_crop_ucrop")
        val (screenWidth, screenHeight) = context.backgroundCropSize()
        val intent = Intent(context, BackgroundCropActivity::class.java).apply {
            putExtra(UCrop.EXTRA_INPUT_URI, sourceUri)
            putExtra(UCrop.EXTRA_OUTPUT_URI, outputUri)
            putExtra(UCrop.EXTRA_ASPECT_RATIO_X, screenWidth.toFloat())
            putExtra(UCrop.EXTRA_ASPECT_RATIO_Y, screenHeight.toFloat())
            putExtra(UCrop.EXTRA_MAX_SIZE_X, screenWidth)
            putExtra(UCrop.EXTRA_MAX_SIZE_Y, screenHeight)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }

        uCropLauncher.launch(intent)
    }

    val launchExternalCrop = { sourceUri: Uri ->
        val uri = createBackgroundCropOutputUri(context, "background_crop_external")
        context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        val (screenWidth, screenHeight) = context.backgroundCropSize()
        val intent = Intent("com.android.camera.action.CROP").apply {
            setDataAndType(uri, "image/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            putExtra("crop", "true")
            putExtra("aspectX", screenWidth)
            putExtra("aspectY", screenHeight)
            putExtra("outputX", screenWidth)
            putExtra("outputY", screenHeight)
            putExtra("return-data", false)
            putExtra(MediaStore.EXTRA_OUTPUT, uri)
        }

        try {
            pendingExternalCropOutputUri = uri
            externalCropLauncher.launch(intent)
        } catch (t: Throwable) {
            Log.e("ThemeSettings", "External Crop failed, fallback to in-app crop", t)
            pendingExternalCropOutputUri = null
            Toast.makeText(
                context,
                externalCropUnavailable,
                Toast.LENGTH_SHORT
            ).show()
            launchInAppCrop(sourceUri)
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            pendingBackgroundImageUri = it
            showCropMethodDialog = true
        }
    }

    SettingsChooseDialog(
        show = showCropMethodDialog,
        title = stringResource(R.string.background_crop_method_title),
        items = listOf(
            stringResource(R.string.background_crop_method_in_app),
            stringResource(R.string.background_crop_method_external)
        ),
        selectedIndex = 0,
        onDismiss = {
            showCropMethodDialog = false
            pendingBackgroundImageUri = null
        },
        onSelectedIndexChange = { index ->
            pendingBackgroundImageUri?.let { sourceUri ->
                when (index) {
                    0 -> launchInAppCrop(sourceUri)
                    1 -> launchExternalCrop(sourceUri)
                }
            }
        }
    )

    // 初始化设置
    LaunchedEffect(Unit) {
        settingsViewModel.initialize(context, systemIsDark)
    }

    // 各种设置对话框
    ThemeSettingsDialogs(
        state = settingsState,
        viewModel = settingsViewModel
    )

    val navigator = LocalNavigator.current

    // Preview card + category grid are pinned; only the selected category is composed below.
    var selectedCategoryKey by rememberSaveable { mutableStateOf(ThemeSettingsCategory.COLORS.name) }
    val selectedCategory = ThemeSettingsCategory.entries
        .firstOrNull { it.name == selectedCategoryKey } ?: ThemeSettingsCategory.COLORS
    var showFullPreview by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    LaunchedEffect(selectedCategory) { listState.scrollToItem(0) }
    val previewModeLabel = settingsState.themeOptions
        .getOrNull(settingsState.themeMode).orEmpty() +
            " · " + settingsState.dynamicPaletteStyle.displayName()

    ThemePreviewDialog(show = showFullPreview, onDismiss = { showFullPreview = false })

    LaunchedEffect(Unit) {
        scrollBehavior.state.heightOffset = scrollBehavior.state.heightOffsetLimit
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                modifier = Modifier
                    .blurEffect(),
                title = {
                    Text(
                        text = stringResource(R.string.theme_settings)
                    )
                },
                navigationIcon = {
                    AppBackButton(
                        onClick = {
                            navigator.pop()
                        }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor =
                        if (ThemeConfig.isEnableBlur)
                            Color.Transparent
                        else
                            MaterialTheme.colorScheme.surfaceContainer.copy(CardConfig.cardAlpha),
                    scrolledContainerColor =
                        if (ThemeConfig.isEnableBlur)
                            Color.Transparent
                        else
                            MaterialTheme.colorScheme.surfaceContainer.copy(CardConfig.cardAlpha),
                ),
                windowInsets = TopAppBarDefaults.windowInsets.add(WindowInsets(left = 12.dp)),
                scrollBehavior = scrollBehavior
            )
        },
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            ThemePreviewCard(
                modeLabel = previewModeLabel,
                onClick = { showFullPreview = true },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp),
            )
            ThemeCategoryGrid(
                selected = selectedCategory,
                onSelect = { selectedCategoryKey = it.name },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp),
            )
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .blurSource()
            ) {
                item(key = selectedCategory.name) {
                    when (selectedCategory) {
                        ThemeSettingsCategory.COLORS -> ColorsSettings(settingsState, settingsViewModel)
                        ThemeSettingsCategory.BACKGROUND -> BackgroundSettings(
                            state = settingsState,
                            viewModel = settingsViewModel,
                            pickImageLauncher = pickImageLauncher,
                            coroutineScope = coroutineScope,
                        )
                        ThemeSettingsCategory.ICON_TEXT -> IconTextSettings(
                            state = settingsState,
                            viewModel = settingsViewModel,
                            coroutineScope = coroutineScope,
                        )
                        ThemeSettingsCategory.HOME_CARD -> {
                            HomeLayoutSettings(
                                homeUiState = homeUiState,
                                homeViewModel = homeViewModel,
                            )
                            HomeCardSettings(homeUiState, homeViewModel)
                        }
                        ThemeSettingsCategory.TOP_BAR -> TopBarSettings()
                        ThemeSettingsCategory.BOTTOM_BAR -> BottomBarSettings(homeUiState, homeViewModel)
                        ThemeSettingsCategory.GESTURES -> GestureSettings(settingsState, settingsViewModel)
                        ThemeSettingsCategory.DETAILS -> DetailsSettings(
                            homeUiState = homeUiState,
                            homeViewModel = homeViewModel,
                            moduleUiState = moduleUiState,
                            moduleViewModel = moduleViewModel,
                        )
                        ThemeSettingsCategory.LANGUAGE -> LanguageSettings(settingsState, settingsViewModel)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(paddingValues.calculateBottomPadding()))
                }
            }
        }
    }
}

private fun PaletteStyle.displayName(): String = when (this) {
    PaletteStyle.TonalSpot -> "Tonal Spot"
    PaletteStyle.Neutral -> "Neutral"
    PaletteStyle.Vibrant -> "Vibrant"
    PaletteStyle.Expressive -> "Expressive"
    PaletteStyle.Rainbow -> "Rainbow"
    PaletteStyle.FruitSalad -> "Fruit Salad"
    PaletteStyle.Monochrome -> "Monochrome"
    PaletteStyle.Fidelity -> "Fidelity"
    PaletteStyle.Content -> "Content"
}

private fun ColorSpec.SpecVersion.displayName(): String = when (this) {
    ColorSpec.SpecVersion.SPEC_2021 -> "Spec 2021"
    ColorSpec.SpecVersion.SPEC_2025 -> "Spec 2025"
}

private fun Context.backgroundCropSize(): Pair<Int, Int> {
    val displayMetrics = resources.displayMetrics
    return displayMetrics.widthPixels to displayMetrics.heightPixels
}

private fun createBackgroundCropOutputUri(context: Context, prefix: String): Uri {
    val outputFile = File(context.cacheDir, "${prefix}_${System.currentTimeMillis()}.jpg").apply {
        parentFile?.mkdirs()
        delete()
        createNewFile()
        deleteOnExit()
    }

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        outputFile
    )
}

@Composable
fun PredictiveBackAnimationWidget(
    uiState: SettingsUiState,
    onSelect: (PredictiveBackAnimation) -> Unit
) {
    SettingsChooseWidget(
        icon = Icons.TwoTone.Animation,
        title = stringResource(R.string.predictive_back_animation),
        items = listOf(
            stringResource(R.string.predictive_back_animation_none),
            stringResource(R.string.predictive_back_animation_aosp),
            stringResource(R.string.predictive_back_animation_miuix),
            stringResource(R.string.predictive_back_animation_scale),
            stringResource(R.string.predictive_back_animation_ksu_classic)
        ),
        selectedIndex = uiState.predictiveBackAnimation.ordinal,
        onSelectedIndexChange = { index ->
            PredictiveBackAnimation.entries.getOrNull(index)?.let(onSelect)
        }
    )
}

@Composable
fun PredictiveBackAnimationDirectionWidget(
    uiState: SettingsUiState,
    onSelect: (PredictiveBackExitDirection) -> Unit
) {
    SettingsChooseWidget(
        icon = Icons.TwoTone.SwapHoriz,
        title = stringResource(R.string.predictive_back_exit_direction),
        items = listOf(
            stringResource(R.string.predictive_back_exit_direction_follow_gesture),
            stringResource(R.string.predictive_back_exit_direction_always_right),
            stringResource(R.string.predictive_back_exit_direction_always_left)
        ),
        selectedIndex = uiState.predictiveBackExitDirection.ordinal,
        onSelectedIndexChange = { index ->
            PredictiveBackExitDirection.entries.getOrNull(index)?.let(onSelect)
        }
    )
}

@Composable
private fun SettingsSurface(shape: Shape, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .renderBackgroundBlur(MaterialTheme.colorScheme.surfaceBright),
        color = if (ThemeConfig.isEnableBlurExp) Color.Transparent else
            MaterialTheme.colorScheme.surfaceBright.copy(alpha = CardConfig.cardAlpha),
        shape = shape,
        content = content,
    )
}

@Composable
private fun ColorsSettings(state: SettingsUiState, viewModel: SettingsViewModel) {
    val context = LocalContext.current
    SegmentedColumn(title = stringResource(R.string.theme_category_colors)) {
        item { shape ->
            SettingsSurface(shape) {
                ThemeModeToggleRow(
                    options = state.themeOptions,
                    selectedIndex = state.themeMode,
                    onSelect = { index -> viewModel.handleThemeModeChange(context, index) },
                )
            }
        }

        item(topPadding = 1.dp) {
            SettingsSwitchWidget(
                icon = Icons.TwoTone.ColorLens,
                title = stringResource(R.string.dynamic_color_title),
                description = stringResource(R.string.dynamic_color_summary),
                checked = state.useDynamicColor,
                onCheckedChange = { viewModel.handleDynamicColorChange(context, it) }
            )
        }

        item(
            visible = !state.useDynamicColor,
            topPadding = 1.dp,
        ) { shape ->
            SettingsSurface(shape) {
                SeedColorRow(state = state, viewModel = viewModel)
            }
        }

        item(
            visible = !state.useDynamicColor,
            topPadding = 1.dp,
        ) {
            ThemeColorSelection(viewModel = viewModel)
        }

        item {
            SettingsChooseWidget(
                icon = Icons.TwoTone.Style,
                title = stringResource(R.string.dynamic_palette_style),
                items = PaletteStyle.entries.map { it.displayName() },
                selectedIndex = PaletteStyle.entries.indexOf(state.dynamicPaletteStyle),
                onSelectedIndexChange = { index ->
                    viewModel.handleDynamicPaletteStyleChange(
                        context,
                        PaletteStyle.entries.getOrElse(index) { PaletteStyle.TonalSpot }
                    )
                }
            )
        }

        item {
            SettingsChooseWidget(
                icon = Icons.TwoTone.DesignServices,
                title = stringResource(R.string.dynamic_color_spec),
                items = ColorSpec.SpecVersion.entries.map { it.displayName() },
                selectedIndex = ColorSpec.SpecVersion.entries.indexOf(state.dynamicColorSpec),
                onSelectedIndexChange = { index ->
                    viewModel.handleDynamicColorSpecChange(
                        context,
                        ColorSpec.SpecVersion.entries.getOrElse(index) {
                            ColorSpec.SpecVersion.SPEC_2021
                        }
                    )
                }
            )
        }
    }
}

/** System / Light / Dark as a connected icon-only toggle group. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ThemeModeToggleRow(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val colors = MaterialTheme.colorScheme
    val icons = listOf(Icons.TwoTone.BrightnessAuto, Icons.TwoTone.LightMode, Icons.TwoTone.DarkMode)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        options.forEachIndexed { index, label ->
            ToggleButton(
                checked = index == selectedIndex,
                onCheckedChange = {
                    if (it) {
                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                        onSelect(index)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .semantics { role = Role.RadioButton },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                colors = ToggleButtonDefaults.colors(
                    checkedContainerColor = colors.primaryContainer,
                    checkedContentColor = colors.onPrimaryContainer,
                    containerColor = colors.surfaceContainerHighest,
                    contentColor = colors.onSurface,
                ),
                contentPadding = PaddingValues(vertical = 14.dp),
            ) {
                Icon(
                    imageVector = icons.getOrElse(index) { Icons.TwoTone.BrightnessAuto },
                    contentDescription = label,
                )
            }
        }
    }
}

private val seedColorOptions: List<Int> = listOf(
    ThemeSeedColors.Default.toArgb(),
    Color(0xFFF44336).toArgb(),
    Color(0xFFE91E63).toArgb(),
    Color(0xFF9C27B0).toArgb(),
    Color(0xFF673AB7).toArgb(),
    Color(0xFF3F51B5).toArgb(),
    Color(0xFF2196F3).toArgb(),
    Color(0xFF00BCD4).toArgb(),
    Color(0xFF009688).toArgb(),
    Color(0xFF4CAF50).toArgb(),
    Color(0xFFFFEB3B).toArgb(),
    Color(0xFFFF9800).toArgb(),
    Color(0xFF795548).toArgb(),
    Color(0xFF607D8B).toArgb(),
)

@Composable
private fun SeedColorRow(state: SettingsUiState, viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val isDark = ThemeConfig.forceDarkMode ?: isSystemInDarkTheme()
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(seedColorOptions) { color ->
            SeedColorTile(
                color = Color(color),
                selected = ThemeConfig.seedColor == color,
                isDark = isDark,
                style = state.dynamicPaletteStyle,
                spec = state.dynamicColorSpec,
                onClick = { viewModel.handleThemeColorChange(context, color) },
            )
        }
    }
}

/** Round-rect tile: top half primaryContainer, bottom half tertiaryContainer, dot / check on top. */
@Composable
private fun SeedColorTile(
    color: Color,
    selected: Boolean,
    isDark: Boolean,
    style: PaletteStyle,
    spec: ColorSpec.SpecVersion,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val scheme = dynamicColorScheme(
        seedColor = color,
        isDark = isDark,
        style = style,
        specVersion = spec,
    )
    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
            onClick()
        },
        shape = RoundedCornerShape(20.dp),
        color = scheme.surfaceContainer,
        modifier = Modifier.size(72.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(48.dp)) {
                drawArc(
                    color = scheme.primaryContainer,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                )
                drawArc(
                    color = scheme.tertiaryContainer,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = true,
                )
            }
            val scale by animateFloatAsState(
                targetValue = if (selected) 1.1f else 1f,
                label = "seedColorScale",
            )
            Box(
                modifier = Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .border(2.dp, scheme.primary, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(scheme.primary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.TwoTone.Check,
                                contentDescription = null,
                                tint = scheme.onPrimary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(scheme.primary, CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
private fun BackgroundSettings(
    state: SettingsUiState,
    viewModel: SettingsViewModel,
    pickImageLauncher: ManagedActivityResultLauncher<String, Uri?>,
    coroutineScope: CoroutineScope,
) {
    SegmentedColumn(title = stringResource(R.string.theme_category_background)) {
        expandableItem(
            expanded = ThemeConfig.customBackgroundUri != null,
            topContent = {
                CustomBackgroundSettings(
                    state = state,
                    viewModel = viewModel,
                    pickImageLauncher = pickImageLauncher,
                )
            },
            bottomContent = {
                backgroundAdjustmentControls(state, viewModel, coroutineScope)
            }
        )

        item(
            topPadding = 1.dp
        ) {
            ModuleBannerAlphaSlider(
                state = state,
                viewModel = viewModel,
                coroutineScope = coroutineScope
            )
        }
    }
}

@Composable
private fun IconTextSettings(
    state: SettingsUiState,
    viewModel: SettingsViewModel,
    coroutineScope: CoroutineScope,
) {
    val context = LocalContext.current
    SegmentedColumn(title = stringResource(R.string.theme_category_icon_text)) {
        item {
            SettingsSwitchWidget(
                icon = Icons.TwoTone.Android,
                title = stringResource(R.string.icon_switch_title),
                description = stringResource(R.string.icon_switch_summary),
                checked = state.useAltIcon,
                onCheckedChange = { viewModel.handleIconChange(context, it) }
            )
        }

        item {
            SettingsSwitchWidget(
                icon = Icons.TwoTone.FormatSize,
                title = stringResource(R.string.settings_custom_monospace_font),
                description = stringResource(R.string.settings_custom_monospace_font_summary),
                checked = state.useBuiltinMonoFont,
                onCheckedChange = { viewModel.handleBuiltinMonospaceFontChange(context, it) }
            )
        }

        item {
            SettingsBaseWidget(
                icon = Icons.TwoTone.FormatSize,
                title = stringResource(R.string.app_dpi_title),
                description = stringResource(R.string.app_dpi_summary),
                onClick = {},
            ) {
                Text(
                    text = viewModel.getDpiFriendlyName(context, state.tempDpi),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        item(
            topPadding = 1.dp,
        ) { shape ->
            Surface(
                modifier = Modifier
                    .clip(shape)
                    .renderBackgroundBlur(MaterialTheme.colorScheme.surfaceBright),
                color = if (ThemeConfig.isEnableBlurExp) Color.Transparent else MaterialTheme.colorScheme.surfaceBright.copy(
                    alpha = CardConfig.cardAlpha
                ),
                shape = shape
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    DpiSliderControls(
                        state = state,
                        viewModel = viewModel,
                        coroutineScope = coroutineScope
                    )
                }
            }
        }
    }
}

@Composable
private fun TopBarSettings() {
    val context = LocalContext.current
    SegmentedColumn(title = stringResource(R.string.theme_category_top_bar)) {
        item(visible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SettingsSwitchWidget(
                icon = Icons.TwoTone.BlurOn,
                title = stringResource(id = R.string.settings_top_bar_blur),
                description = stringResource(id = R.string.settings_top_bar_blur_summary),
                checked = ThemeConfig.isEnableBlur,
                onCheckedChange = { isChecked ->
                    BackgroundManager.saveEnableBlur(context, isChecked)
                    if (!isChecked && !ThemeConfig.isEnableLiquidGlass)
                        BackgroundManager.saveEnableBlurExp(context, false)
                }
            )
        }
    }
}

@Composable
private fun BottomBarSettings(
    homeUiState: HomeUiState,
    homeViewModel: HomeViewModel,
) {
    val context = LocalContext.current
    SegmentedColumn(title = stringResource(R.string.theme_category_bottom_bar)) {
        item(visible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SettingsSwitchWidget(
                icon = Icons.TwoTone.BlurOn,
                title = stringResource(id = R.string.settings_bottom_bar_liquid_glass),
                description = stringResource(id = R.string.settings_bottom_bar_liquid_glass_summary),
                checked = ThemeConfig.isEnableLiquidGlass,
                onCheckedChange = { isChecked ->
                    BackgroundManager.saveEnableLiquidGlass(context, isChecked)
                    if (!isChecked && !ThemeConfig.isEnableBlur)
                        BackgroundManager.saveEnableBlurExp(context, false)
                }
            )
        }

        item {
            SettingsSwitchWidget(
                icon = Icons.TwoTone.Pin,
                title = stringResource(R.string.navigation_bar_badge),
                description = stringResource(R.string.navigation_bar_badge_summary),
                checked = homeUiState.showNavigationBarBadge,
                onCheckedChange = { homeViewModel.handleNavigationBarBadgeChange(context, it) }
            )
        }
    }
}

@Composable
private fun GestureSettings(
    settingsState: SettingsUiState,
    settingsViewModel: SettingsViewModel,
) {
    val context = LocalContext.current
    SegmentedColumn(title = stringResource(R.string.predictive_back_settings)) {
        item {
            PredictiveBackAnimationWidget(settingsState) { animation ->
                settingsViewModel.setPredictiveBackAnimation(context, animation)
            }
        }
        item(
            visible = settingsState.predictiveBackAnimation == PredictiveBackAnimation.Scale ||
                    settingsState.predictiveBackAnimation == PredictiveBackAnimation.AOSP
        ) {
            PredictiveBackAnimationDirectionWidget(settingsState) { direction ->
                settingsViewModel.setPredictiveBackExitDirection(context, direction)
            }
        }
    }

    SegmentedColumn(title = stringResource(R.string.settings_pager_gesture_mode)) {
        item {
            SettingsSwitchWidget(
                icon = Icons.TwoTone.Swipe,
                title = stringResource(R.string.settings_enable_swipe_dismiss),
                description = stringResource(R.string.settings_enable_swipe_dismiss_summary),
                checked = settingsState.enableSwipeDismiss,
                onCheckedChange = { settingsViewModel.handleSwipeDismissChange(context, it) }
            )
        }

        item {
            SettingsChooseWidget(
                icon = Icons.TwoTone.ViewCarousel,
                title = stringResource(R.string.settings_pager_gesture_mode),
                items = listOf(
                    stringResource(R.string.settings_pager_gesture_native),
                    stringResource(R.string.settings_pager_gesture_cross_axis),
                    stringResource(R.string.settings_pager_gesture_ios_like),
                ),
                selectedIndex = settingsState.pagerGestureMode.coerceIn(0, 2),
                onSelectedIndexChange = { index ->
                    settingsViewModel.handlePagerGestureModeChange(context, index)
                }
            )
        }
    }
}

@Composable
private fun LanguageSettings(
    state: SettingsUiState,
    viewModel: SettingsViewModel,
) {
    SegmentedColumn(title = stringResource(R.string.theme_category_language)) {
        item {
            LanguageSetting(state = state, viewModel = viewModel)
        }
    }
}

@Composable
private fun HomeLayoutSettings(
    homeUiState: HomeUiState,
    homeViewModel: HomeViewModel,
) {
    val context = LocalContext.current
    val bannerCropLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { UCrop.getOutput(it) }?.let { cropped ->
                homeViewModel.handleHomeBannerPicked(context, cropped) { ok ->
                    if (!ok) {
                        Toast.makeText(context, R.string.home_banner_failed, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            Toast.makeText(context, R.string.home_banner_failed, Toast.LENGTH_SHORT).show()
        }
    }
    val bannerPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { source ->
            val aspect = HomeBannerStorage.aspect
            val maxWidth = HomeBannerStorage.CROP_MAX_WIDTH
            val intent = Intent(context, BackgroundCropActivity::class.java).apply {
                putExtra(UCrop.EXTRA_INPUT_URI, source)
                putExtra(UCrop.EXTRA_OUTPUT_URI, createBackgroundCropOutputUri(context, "home_banner_crop"))
                putExtra(UCrop.EXTRA_ASPECT_RATIO_X, aspect)
                putExtra(UCrop.EXTRA_ASPECT_RATIO_Y, 1f)
                putExtra(UCrop.EXTRA_MAX_SIZE_X, maxWidth)
                putExtra(UCrop.EXTRA_MAX_SIZE_Y, (maxWidth / aspect).toInt())
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
            bannerCropLauncher.launch(intent)
        }
    }
    SegmentedColumn(title = stringResource(R.string.home_layout_section)) {
        item {
            SettingsChooseWidget(
                icon = Icons.TwoTone.Dashboard,
                title = stringResource(R.string.home_layout_style),
                items = listOf(
                    stringResource(R.string.home_layout_circle),
                    stringResource(R.string.home_layout_dashboard),
                ),
                selectedIndex = HomeLayoutStyle.supported.indexOf(homeUiState.homeLayoutStyle)
                    .coerceAtLeast(0),
                onSelectedIndexChange = { index ->
                    homeViewModel.handleHomeLayoutStyleChange(
                        context,
                        HomeLayoutStyle.supported.getOrElse(index) { HomeLayoutStyle.DEFAULT }
                    )
                }
            )
        }

        item(visible = homeUiState.homeLayoutStyle == HomeLayoutStyle.DASHBOARD) {
            SettingsBaseWidget(
                icon = Icons.TwoTone.Image,
                title = stringResource(R.string.home_banner_pick),
                description = stringResource(
                    if (homeUiState.homeBannerFile != null) R.string.home_banner_set
                    else R.string.home_banner_pick_summary
                ),
                onClick = { bannerPicker.launch("image/*") }
            )
        }

        item(
            visible = homeUiState.homeLayoutStyle == HomeLayoutStyle.DASHBOARD &&
                homeUiState.homeBannerFile != null
        ) {
            SettingsBaseWidget(
                icon = Icons.TwoTone.Delete,
                title = stringResource(R.string.home_banner_reset),
                onClick = { homeViewModel.handleHomeBannerReset(context) }
            )
        }
    }
}

@Composable
private fun HomeCardSettings(
    homeUiState: HomeUiState,
    homeViewModel: HomeViewModel,
) {
    val context = LocalContext.current
    SegmentedColumn(title = stringResource(R.string.theme_category_home_card)) {
        item {
            SettingsSwitchWidget(
                icon = Icons.TwoTone.Brush,
                title = stringResource(R.string.simple_mode),
                description = stringResource(R.string.simple_mode_summary),
                checked = homeUiState.isSimpleMode,
                onCheckedChange = { homeViewModel.handleSimpleModeChange(context, it) }
            )
        }

        item {
            SettingsSwitchWidget(
                icon = Icons.TwoTone.Badge,
                title = stringResource(R.string.home_card_icons),
                description = stringResource(R.string.home_card_icons_summary),
                checked = homeUiState.showHomeCardIcons,
                onCheckedChange = { homeViewModel.handleHomeCardIconsChange(context, it) }
            )
        }
    }
}

@Composable
private fun DetailsSettings(
    homeUiState: HomeUiState,
    homeViewModel: HomeViewModel,
    moduleUiState: ModuleUiState,
    moduleViewModel: ModuleViewModel,
) {
    val context = LocalContext.current
    SegmentedColumn(title = stringResource(R.string.theme_category_details)) {
        item {
            SettingsSwitchWidget(
                icon = Icons.TwoTone.Info,
                title = stringResource(R.string.show_more_module_info),
                description = stringResource(R.string.show_more_module_info_summary),
                checked = moduleUiState.showMoreModuleInfo,
                onCheckedChange = { moduleViewModel.handleShowMoreModuleInfoChange(context, it) }
            )
        }

        hideOptionsSettings(homeUiState, moduleUiState, homeViewModel, moduleViewModel)
    }
}

private fun SegmentedColumnScope.hideOptionsSettings(
    homeUiState: HomeUiState,
    moduleUiState: ModuleUiState,
    homeViewModel: HomeViewModel,
    moduleViewModel: ModuleViewModel,
) {
    item {
        // 隐藏模块数量等信息
        SettingsSwitchWidget(
            icon = Icons.TwoTone.VisibilityOff,
            title = stringResource(R.string.hide_other_info),
            description = stringResource(R.string.hide_other_info_summary),
            checked = homeUiState.isHideOtherInfo,
            onCheckedChange = homeViewModel::handleHideOtherInfoChange
        )
    }

    item {
        // SuSFS 状态信息
        SettingsSwitchWidget(
            icon = Icons.TwoTone.VisibilityOff,
            title = stringResource(R.string.hide_susfs_status),
            description = stringResource(R.string.hide_susfs_status_summary),
            checked = homeUiState.isHideSusfsStatus,
            onCheckedChange = homeViewModel::handleHideSusfsStatusChange
        )
    }

    item {
        // Zygisk 实现状态信息
        SettingsSwitchWidget(
            icon = Icons.TwoTone.VisibilityOff,
            title = stringResource(R.string.hide_zygisk_implement),
            description = stringResource(R.string.hide_zygisk_implement_summary),
            checked = homeUiState.isHideZygiskImplement,
            onCheckedChange = homeViewModel::handleHideZygiskImplementChange
        )
    }

    item {
        // 元模块实现状态信息
        SettingsSwitchWidget(
            icon = Icons.TwoTone.VisibilityOff,
            title = stringResource(R.string.hide_meta_module_implement),
            description = stringResource(R.string.hide_meta_module_implement_summary),
            checked = homeUiState.isHideMetaModuleImplement,
            onCheckedChange = homeViewModel::handleHideMetaModuleImplementChange
        )
    }

    item {
        // 隐藏链接信息
        SettingsSwitchWidget(
            icon = Icons.TwoTone.VisibilityOff,
            title = stringResource(R.string.hide_link_card),
            description = stringResource(R.string.hide_link_card_summary),
            checked = homeUiState.isHideLinkCard,
            onCheckedChange = homeViewModel::handleHideLinkCardChange
        )
    }

    item {
        // 隐藏标签行
        SettingsSwitchWidget(
            icon = Icons.TwoTone.VisibilityOff,
            title = stringResource(R.string.hide_tag_card),
            description = stringResource(R.string.hide_tag_card_summary),
            checked = moduleUiState.isHideTagRow,
            onCheckedChange = moduleViewModel::handleHideTagRowChange
        )
    }
}

@Composable
private fun ThemeColorSelection(viewModel: SettingsViewModel) {
    SettingsBaseWidget(
        icon = Icons.TwoTone.Palette,
        title = stringResource(R.string.theme_color),
        description = ThemeConfig.seedColor.toSeedColorHex(),
        onClick = { viewModel.setThemeColorDialogVisible(true) },
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(Color(ThemeConfig.seedColor))
        )
    }
}

private fun Int.toSeedColorHex(): String = "#%06X".format(this and 0x00FFFFFF)

@Composable
private fun DpiSliderControls(
    state: SettingsUiState,
    viewModel: SettingsViewModel,
    coroutineScope: CoroutineScope
) {
    val context = LocalContext.current
    val confirmDialog = rememberConfirmDialog()
    val dpiConfirmTitle = stringResource(R.string.dpi_confirm_title)
    val dpiConfirmMessage =
        stringResource(R.string.dpi_confirm_message, state.currentDpi, state.tempDpi)
    val confirmText = stringResource(R.string.confirm)
    val cancelText = stringResource(R.string.cancel)

    val sliderValue by animateFloatAsState(
        targetValue = state.tempDpi.toFloat(),
        label = "DPI Slider Animation"
    )

    KeyPointSlider(
        value = sliderValue,
        onValueChange = { newValue ->
            viewModel.updateTempDpi(newValue.toInt())
        },
        modifier = Modifier.fillMaxWidth(),
        valueRange = 160f..600f,
        keyPoints = state.dpiPresets.map { (_, dpi) -> dpi.toFloat() },
    )

    // DPI 预设按钮行
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
    ) {
        state.dpiPresets.forEach { (name, dpi) ->
            val isSelected = state.tempDpi == dpi
            val buttonColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceBright

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(buttonColor)
                    .clickable {
                        viewModel.updateTempDpi(dpi)
                    }
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    Text(
        text = if (state.isDpiCustom)
            "${stringResource(R.string.dpi_size_custom)}: ${state.tempDpi}"
        else
            "${viewModel.getDpiFriendlyName(context, state.tempDpi)}: ${state.tempDpi}",
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(top = 8.dp)
    )

    Button(
        onClick = {
            coroutineScope.launch {
                val confirmResult = confirmDialog.awaitConfirm(
                    title = dpiConfirmTitle,
                    content = dpiConfirmMessage,
                    confirm = confirmText,
                    dismiss = cancelText
                )

                if (confirmResult != ConfirmResult.Confirmed) return@launch

                viewModel.handleDpiApply(context)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        enabled = state.tempDpi != state.currentDpi
    ) {
        Icon(
            Icons.TwoTone.Check,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(R.string.dpi_apply_settings))
    }
}

@Composable
private fun CustomBackgroundSettings(
    state: SettingsUiState,
    viewModel: SettingsViewModel,
    pickImageLauncher: ManagedActivityResultLauncher<String, Uri?>,
) {
    val context = LocalContext.current
    // TODO Portrait/Landscape wallpaper split

    SettingsSwitchWidget(
        icon = Icons.TwoTone.Wallpaper,
        title = stringResource(id = R.string.settings_custom_background),
        description = stringResource(id = R.string.settings_custom_background_summary),
        checked = state.isCustomBackgroundEnabled,
        onCheckedChange = { isChecked ->
            if (isChecked) {
                pickImageLauncher.launch("image/*")
            } else {
                viewModel.handleRemoveCustomBackground(context)
            }
        },
    )
}

private fun SegmentedColumnScope.backgroundAdjustmentControls(
    state: SettingsUiState,
    viewModel: SettingsViewModel,
    coroutineScope: CoroutineScope,
) {
    item(
        topPadding = 1.dp
    ) {
        AlphaSlider(
            state = state,
            viewModel = viewModel,
            coroutineScope = coroutineScope
        )
    }

    item(
        topPadding = 1.dp
    ) {
        DimSlider(
            state = state,
            viewModel = viewModel,
            coroutineScope = coroutineScope
        )
    }

    item(
        visible = ThemeConfig.isEnableBlur || ThemeConfig.isEnableLiquidGlass,
        topPadding = 1.dp,
    ) {
        val context = LocalContext.current

        SettingsSwitchWidget(
            icon = Icons.TwoTone.Draw,
            title = stringResource(id = R.string.settings_exp_draw_background_to_blur),
            description = stringResource(id = R.string.settings_exp_draw_background_to_blur_description),
            isError = true,
            checked = ThemeConfig.isEnableBlurExp,
            onCheckedChange = { isChecked ->
                BackgroundManager.saveEnableBlurExp(context, isChecked)
            }
        )
    }

    item(
        visible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && state.useDynamicColor,
        topPadding = 1.dp,
    ) {
        val context = LocalContext.current

        SettingsSwitchWidget(
            icon = Icons.TwoTone.FormatColorFill,
            title = stringResource(id = R.string.settings_config_use_custom_background_seed_color),
            description = stringResource(id = R.string.settings_config_use_custom_background_seed_color_summary),
            checked = ThemeConfig.isUseBackgroundSeedColor,
            onCheckedChange = { isChecked ->
                BackgroundManager.saveUseBackgroundSeedColor(context, isChecked)
            }
        )
    }

    item(
        visible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
        topPadding = 1.dp,
    ) {
        val context = LocalContext.current

        SettingsSwitchWidget(
            icon = Icons.TwoTone.Contrast,
            title = stringResource(id = R.string.settings_custom_enable_high_contrast),
            description = stringResource(id = R.string.settings_custom_enable_high_contrast_summary),
            checked = ThemeConfig.isHighContrastMode,
            onCheckedChange = { isChecked ->
                BackgroundManager.saveEnableHighContrastMode(context, isChecked)
            }
        )
    }
}

@Composable
private fun AlphaSlider(
    state: SettingsUiState,
    viewModel: SettingsViewModel,
    coroutineScope: CoroutineScope
) {
    val context = LocalContext.current
    SettingsBaseWidget(
        icon = Icons.TwoTone.Opacity,
        title = stringResource(R.string.settings_card_alpha),
        descriptionColumnContent = {
            val alphaSliderValue by animateFloatAsState(
                targetValue = state.cardAlpha,
                label = "Alpha Slider Animation"
            )

            KeyPointSlider(
                value = alphaSliderValue,
                onValueChange = { newValue ->
                    viewModel.handleCardAlphaChange(context, newValue)
                },
                onValueChangeFinished = {
                    coroutineScope.launch(Dispatchers.IO) {
                        viewModel.saveCardConfig(context)
                    }
                },
                valueRange = 0f..1f,
                keyPoints = listOf(0.25f, 0.5f, 0.75f),
            )
        }
    ) {
        Box(contentAlignment = Alignment.CenterEnd) {
            Text( // Some stupid way to solve measure problem
                text = "100%",
                style = MaterialTheme.typography.labelMediumEmphasized,
                modifier = Modifier.alpha(0f)
            )
            Text(
                text = "${(state.cardAlpha * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelMediumEmphasized
            )
        }
    }
}

@Composable
private fun ModuleBannerAlphaSlider(
    state: SettingsUiState,
    viewModel: SettingsViewModel,
    coroutineScope: CoroutineScope
) {
    val context = LocalContext.current
    SettingsBaseWidget(
        icon = Icons.TwoTone.Opacity,
        title = stringResource(R.string.settings_module_banner_alpha),
        descriptionColumnContent = {
            val alphaSliderValue by animateFloatAsState(
                targetValue = state.moduleBannerAlpha,
                label = "Module Banner Alpha Slider Animation"
            )

            KeyPointSlider(
                value = alphaSliderValue,
                onValueChange = { newValue ->
                    viewModel.handleModuleBannerAlphaChange(context, newValue)
                },
                onValueChangeFinished = {
                    coroutineScope.launch(Dispatchers.IO) {
                        viewModel.saveCardConfig(context)
                    }
                },
                valueRange = 0f..1f,
                keyPoints = listOf(0.25f, 0.5f, 0.75f),
            )
        }
    ) {
        Box(contentAlignment = Alignment.CenterEnd) {
            Text( // Some stupid way to solve measure problem
                text = "100%",
                style = MaterialTheme.typography.labelMediumEmphasized,
                modifier = Modifier.alpha(0f)
            )
            Text(
                text = "${(state.moduleBannerAlpha * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelMediumEmphasized
            )
        }
    }
}

@Composable
private fun DimSlider(
    state: SettingsUiState,
    viewModel: SettingsViewModel,
    coroutineScope: CoroutineScope
) {
    val context = LocalContext.current
    SettingsBaseWidget(
        icon = Icons.TwoTone.LightMode,
        title = stringResource(R.string.settings_background_dim),
        descriptionColumnContent = {
            val dimSliderValue by animateFloatAsState(
                targetValue = state.backgroundDim,
                label = "Dim Slider Animation"
            )

            KeyPointSlider(
                value = dimSliderValue,
                onValueChange = { newValue ->
                    viewModel.handleBackgroundDimChange(context, newValue)
                },
                onValueChangeFinished = {
                    coroutineScope.launch(Dispatchers.IO) {
                        viewModel.saveCardConfig(context)
                    }
                },
                valueRange = 0f..1f,
                keyPoints = listOf(0.25f, 0.5f, 0.75f),
            )
        }
    ) {
        Box(contentAlignment = Alignment.CenterEnd) {
            Text( // Some stupid way to solve measure problem
                text = "100%",
                style = MaterialTheme.typography.labelMediumEmphasized,
                modifier = Modifier.alpha(0f)
            )

            Text(
                text = "${(state.backgroundDim * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelMediumEmphasized,
            )
        }
    }
}

@Composable
private fun LanguageSetting(state: SettingsUiState, viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val language = stringResource(id = R.string.settings_language)
    val languageSystemDefault = stringResource(R.string.language_system_default)

    // Compute display name based on current app locale
    val currentLanguageDisplay = remember(state.currentAppLocale) {
        val locale = state.currentAppLocale
        if (locale != null) {
            locale.getDisplayName(locale)
        } else {
            languageSystemDefault
        }
    }

    SettingsJumpPageWidget(
        icon = Icons.TwoTone.Translate,
        title = language,
        description = currentLanguageDisplay,
        onClick = { viewModel.setLanguageDialogVisible(true) }
    )

    // Language Selection Dialog
    if (state.showLanguageDialog) {
        LanguageSelectionDialog(
            onLanguageSelected = {
                // Update local state immediately
                viewModel.refreshCurrentLocale(context)
                // Apply locale change immediately for Android < 13
                restartActivity(context)
            },
            onDismiss = { viewModel.setLanguageDialogVisible(false) }
        )
    }
}
