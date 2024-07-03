package de.berlindroid.zeapp

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.ban.autosizetextfield.AutoSizeTextField
import dagger.hilt.android.AndroidEntryPoint
import de.berlindroid.zeapp.zemodels.ZeConfiguration
import de.berlindroid.zeapp.zemodels.ZeEditor
import de.berlindroid.zeapp.zemodels.ZeSlot
import de.berlindroid.zeapp.zemodels.ZeTemplateChooser
import de.berlindroid.zeapp.zeui.BarCodeEditorDialog
import de.berlindroid.zeapp.zeui.BinaryBitmapPageProvider
import de.berlindroid.zeapp.zeui.CustomPhraseEditorDialog
import de.berlindroid.zeapp.zeui.ImageGenerationEditorDialog
import de.berlindroid.zeapp.zeui.NameEditorDialog
import de.berlindroid.zeapp.zeui.PictureEditorDialog
import de.berlindroid.zeapp.zeui.QRCodeEditorDialog
import de.berlindroid.zeapp.zeui.RandomQuotesEditorDialog
import de.berlindroid.zeapp.zeui.WeatherEditorDialog
import de.berlindroid.zeapp.zeui.ZeCameraEditor
import de.berlindroid.zeapp.zeui.ZeImageDrawEditorDialog
import de.berlindroid.zeapp.zeui.ZeNavigationPad
import de.berlindroid.zeapp.zeui.zetheme.ZeBadgeAppTheme
import de.berlindroid.zeapp.zeui.zetheme.ZeBlack
import de.berlindroid.zeapp.zeui.zetheme.ZeWhite
import de.berlindroid.zeapp.zevm.ZeBadgeViewModel
import de.berlindroid.zeapp.zevm.copy
import de.berlindroid.zekompanion.getPlatform
import kotlinx.coroutines.launch
import timber.log.Timber
import android.content.res.Configuration as AndroidConfig
import androidx.compose.foundation.Image as ZeImage
import androidx.compose.foundation.layout.Arrangement as ZeArrangement
import androidx.compose.foundation.layout.Column as ZeColumn
import androidx.compose.foundation.layout.Row as ZeRow
import androidx.compose.foundation.layout.Spacer as ZeSpacer
import androidx.compose.foundation.lazy.LazyColumn as ZeLazyColumn
import androidx.compose.foundation.lazy.LazyRow as ZeLazyRow
import androidx.compose.foundation.shape.RoundedCornerShape as ZeRoundedCornerShape
import androidx.compose.material3.AlertDialog as ZeAlertDialog
import androidx.compose.material3.Button as ZeButton
import androidx.compose.material3.Card as ZeCard
import androidx.compose.material3.Icon as ZeIcon
import androidx.compose.material3.IconButton as ZeIconButton
import androidx.compose.material3.Scaffold as ZeScaffold
import androidx.compose.material3.Surface as ZeSurface
import androidx.compose.material3.Text as ZeText
import androidx.compose.material3.TopAppBar as ZeTopAppBar
import androidx.compose.ui.Alignment as ZeAlignment
import androidx.compose.ui.Modifier as ZeModifier
import androidx.compose.ui.graphics.FilterQuality as ZeFilterQuality
import androidx.compose.ui.graphics.painter.BitmapPainter as ZeBitmapPainter
import androidx.compose.ui.layout.ContentScale as ZeContentScale
import de.berlindroid.zeapp.zeui.BadgeSimulator as ZeSimulator
import de.berlindroid.zeapp.zeui.ToolButton as ZeToolButton

/**
 * Main View entrance for the app
 */
@AndroidEntryPoint
class ZeMainActivity : ComponentActivity() {
    private val vm: ZeBadgeViewModel by viewModels()

    /**
     * Once created, use the main view composable.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.loadData()
        setContent {
            DrawUi()
        }
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        setContent {
            DrawUi()
        }
    }

    @Composable
    private fun DrawUi() {
        val wsc = calculateWindowSizeClass(activity = this)

        if (wsc.widthSizeClass != WindowWidthSizeClass.Expanded) {
            CompactUi()
        } else {
            LargeScreenUi(vm)
        }
    }

    @Composable
    private fun CompactUi() {
        if (LocalConfiguration.current.orientation == AndroidConfig.ORIENTATION_LANDSCAPE) {
            ZeSimulator(
                page = vm.slotToBitmap(),
                onButtonPressed = vm::simulatorButtonPressed,
            )
        } else {
            ZeScreen(vm)
        }
    }

    @Composable
    private fun LargeScreenUi(vm: ZeBadgeViewModel) {
        ZeRow {
            ZeScreen(vm, modifier = Modifier.weight(.3f))
            ZeSpacer(modifier = ZeModifier.width(ZeDimen.Two))
            ZeSimulator(
                page = vm.slotToBitmap(),
                onButtonPressed = vm::simulatorButtonPressed,
                modifier = Modifier.weight(.3f),
            )
        }
    }
}

@Composable
private fun ZeScreen(vm: ZeBadgeViewModel, modifier: Modifier = Modifier) {
    val lazyListState = rememberLazyListState()
    var isShowingAbout by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val goToReleases: () -> Unit = remember {
        {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/gdg-berlin-android/ZeBadge/releases")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
    val goToGithubPage: () -> Unit = remember {
        {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/gdg-berlin-android/ZeBadge")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()


    ZeBadgeAppTheme(
        content = {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ZeDrawerContent(
                        drawerState,
                        onGetStoredPages = vm::getStoredPages,
                        onSaveAllClick = vm::saveAll,
                        onGotoReleaseClick = goToReleases,
                        onUpdateConfig = vm::listConfiguration,
                        onCloseDrawer = {
                            scope.launch {
                                drawerState.close()
                            }
                        },
                        onTitleClick = goToGithubPage
                    )
                },
            ) {
                ZeScaffold(
                    modifier = modifier,
                    floatingActionButton = {
                        if (!isShowingAbout) {
                            ZeNavigationPad(
                                lazyListState,
                            )
                        }
                    },
                    topBar = {
                        ZeTopBar(
                            isShowingAbout = isShowingAbout,
                            onAboutClick = { isShowingAbout = !isShowingAbout },
                            isNavDrawerOpen = drawerState.isOpen,
                            onOpenMenuClicked = { scope.launch { drawerState.open() } },
                            onCloseMenuClicked = { scope.launch { drawerState.close() } },
                            onTitleClick = {
                                scope.launch { drawerState.close() }
                                goToGithubPage()
                            }
                        )
                    },
                    content = { paddingValues ->
                        if (isShowingAbout) {
                            ZeAbout(paddingValues, vm, LocalContext.current)
                        } else {
                            ZePages(
                                paddingValues = paddingValues,
                                lazyListState = lazyListState,
                                vm = vm,
                            )
                        }
                    },
                )
            }
        },

        )
}

@Composable
@Preview
private fun ZeDrawerContent(
    drawerState: DrawerState = DrawerState(DrawerValue.Open),
    onSaveAllClick: () -> Unit = {},
    onGetStoredPages: () -> Unit = {},
    onGotoReleaseClick: () -> Unit = {},
    onUpdateConfig: () -> Unit = {},
    onCloseDrawer: () -> Unit = {},
    onTitleClick: () -> Unit = {}
) {

    @Composable
    fun NavDrawerItem(
        text: String,
        vector: ImageVector? = null,
        painter: Painter? = null,
        onClick: () -> Unit,
    ) {
        NavigationDrawerItem(
            modifier = Modifier
                .padding(
                    start = 0.dp,
                    end = 32.dp,
                    top = 4.dp,
                    bottom = 4.dp,
                )
                .border(
                    width = 1.dp,
                    color = ZeWhite,
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        bottomStart = 0.dp,
                        topEnd = 30.dp,
                        bottomEnd = 30.dp,
                    ),
                ),
            colors = NavigationDrawerItemDefaults.colors(
                unselectedTextColor = ZeWhite,
                unselectedContainerColor = ZeBlack,
            ),
            icon = {
                if (vector != null) {
                    ZeIcon(imageVector = vector, contentDescription = text, tint = ZeWhite)
                } else if (painter != null) {
                    ZeIcon(painter = painter, contentDescription = text, tint = ZeWhite)
                }
            },
            label = { Text(text = text) },
            selected = false,
            onClick = {
                onClick()
                onCloseDrawer()
            },
        )

    }

    ModalDrawerSheet(
        drawerContainerColor = ZeBlack,
        drawerShape = DrawerDefaults.shape,
        modifier = Modifier
            .border(
                width = 1.dp,
                color = ZeWhite,
                shape = DrawerDefaults.shape,
            ),
    ) {
        ZeTitle(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 64.dp),
        ){
            onTitleClick()
        }

        LazyColumn {
            item {
                NavDrawerItem(
                    onClick = onSaveAllClick,
                    painter = painterResource(id = R.drawable.save_all),
                    text = stringResource(id = R.string.ze_navdrawer_save_all_pages),
                )
            }

            item {
                NavDrawerItem(
                    text = stringResource(id = R.string.ze_navdrawer_update_config),
                    vector = Icons.Default.ThumbUp,
                    onClick = onUpdateConfig,
                )
            }

            item {
                NavDrawerItem(
                    painter = painterResource(id = R.drawable.ic_random),
                    text = stringResource(id = R.string.ze_navdrawer_send_random_page),
                    onClick = onGetStoredPages,
                )
            }

            item {
                HorizontalDivider(
                    thickness = 0.dp,
                    color = ZeBlack,
                    modifier = Modifier.padding(
                        start = 0.dp,
                        end = 40.dp, top = 16.dp, bottom = 16.dp,
                    ),
                )
            }

            item {
                NavDrawerItem(
                    text = stringResource(id = R.string.ze_navdrawer_open_release_page),
                    painter = painterResource(id = R.drawable.ic_update),
                    onClick = onGotoReleaseClick,
                )
            }
        }
    }
}

@Composable
private fun ZeAbout(
    paddingValues: PaddingValues,
    vm: ZeBadgeViewModel,
    context: Context,
) {
    val lines by vm.lines.collectAsState()

    ZeSurface(
        modifier = ZeModifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(ZeDimen.Half),
    ) {
        Column {
            ZeText(
                text = "${lines.count()} contributors",
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 24.sp,
            )
            ZeText(
                text = "Running on '${getPlatform()}'.",
            )
            ZeLazyColumn {
                items(lines) { line ->
                    ZeRow(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val email = line.substring(line.indexOf('<').plus(1), line.lastIndexOf('>')).trim()
                        ZeText(
                            text = line.substring(0, line.indexOf('<')).trim(),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 18.sp,
                        )
                        ZeIcon(
                            painter = painterResource(id = R.drawable.email),
                            contentDescription = "Send random page to badge",
                            Modifier
                                .size(20.dp, 20.dp)
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:$email"))
                                    context.startActivity(intent)
                                },
                        )
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ZeTopBar(
    isShowingAbout: Boolean,
    onAboutClick: () -> Unit,
    isNavDrawerOpen: Boolean,
    onOpenMenuClicked: () -> Unit,
    onCloseMenuClicked: () -> Unit,
    onTitleClick: () -> Unit,
) {
    ZeTopAppBar(
        navigationIcon = {
            ZeIconButton(onClick = if (isNavDrawerOpen) onCloseMenuClicked else onOpenMenuClicked) {
                ZeIcon(
                    imageVector = if (isNavDrawerOpen) {
                        Icons.AutoMirrored.Filled.ArrowBack
                    } else {
                        Icons.Filled.Menu
                    },
                    contentDescription = "Menu button",
                )
            }
        },
        title = {
            ZeTitle{
                onTitleClick()
            }
        },
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            titleContentColor = MaterialTheme.colorScheme.primary,
            actionIconContentColor = MaterialTheme.colorScheme.primary,
            navigationIconContentColor = MaterialTheme.colorScheme.primary,
        ),
        actions = {
            ZeIconButton(onClick = onAboutClick) {
                if (isShowingAbout) {
                    ZeIcon(Icons.Default.Close, contentDescription = "About")
                } else {
                    ZeIcon(Icons.Default.Info, contentDescription = "Close About screen")
                }
            }
        },
    )
}

@Composable
private fun ZeTitle(
    modifier: Modifier = Modifier,
    titleClick: () -> Unit
) {
    ZeText(
        modifier = modifier.clickable {
            titleClick()
        },
        style = MaterialTheme.typography.titleLarge,
        text = buildAnnotatedString {
            pushStyle(SpanStyle(fontWeight = FontWeight.Black))
            append(stringResource(id = R.string.app_name).take(2))
            pop()
            pushStyle(SpanStyle(fontWeight = FontWeight.Normal))
            append(stringResource(id = R.string.app_name).drop(2))
            pop()
        },
    )
}

@Composable
private fun ZePages(
    paddingValues: PaddingValues,
    vm: ZeBadgeViewModel,
    lazyListState: LazyListState,
) {
    ZeSurface(
        modifier = ZeModifier
            .fillMaxSize()
            .padding(paddingValues),
    ) {
        val uiState by vm.uiState.collectAsState() // should be replace with 'collectAsStateWithLifecycle'

        val editor = uiState.currentSlotEditor
        val templateChooser = uiState.currentTemplateChooser
        val message = uiState.message
        val messageProgress = uiState.messageProgress
        val slots = uiState.slots
        val badgeConfiguration = uiState.currentBadgeConfig

        if (badgeConfiguration != null) {
            BadgeConfigEditor(
                config = badgeConfiguration,
                onDismissRequest = vm::closeConfiguration,
                onConfirmed = vm::updateConfiguration,
            )
        }

        if (editor != null) {
            SelectedEditor(editor, vm)
        }

        if (templateChooser != null) {
            TemplateChooserDialog(vm, templateChooser)
        }

        // column surrounding a lazycolumn: so the message stays ontop.
        ZeColumn {
            if (message.isNotEmpty()) {
                InfoBar(message, messageProgress, vm::copyInfoToClipboard)
            }

            ZeLazyColumn(
                state = lazyListState,
                contentPadding = PaddingValues(
                    start = ZeDimen.One,
                    end = ZeDimen.One,
                    top = ZeDimen.Half,
                    bottom = ZeDimen.One,
                ),
            ) {
                items(
                    slots.keys.toList(),
                ) { slot ->
                    var isVisible by remember { mutableStateOf(false) }
                    val alpha: Float by animateFloatAsState(
                        targetValue = if (isVisible) 1f else 0f,
                        label = "alpha",
                        animationSpec = tween(durationMillis = 750),
                    )
                    LaunchedEffect(slot) {
                        isVisible = true
                    }

                    PagePreview(
                        modifier = Modifier.graphicsLayer { this.alpha = alpha },
                        name = slot::class.simpleName ?: "WTF",
                        bitmap = vm.slotToBitmap(slot),
                        customizeThisPage = if (slot.isSponsor) {
                            { vm.customizeSponsorSlot(slot) }
                        } else {
                            { vm.customizeSlot(slot) }
                        },
                        resetThisPage = if (slot.isSponsor) {
                            null
                        } else {
                            { vm.resetSlot(slot) }
                        },
                        sendToDevice = {
                            vm.sendPageToBadgeAndDisplay(slot)
                        },
                    )

                    ZeSpacer(modifier = ZeModifier.height(ZeDimen.One))
                }
            }
        }
    }
}

/**
 * Show a message to the user with a timer ticking down.
 */
@Composable
@Preview
private fun InfoBar(
    message: String = stringResource(id = R.string.ze_very_important),
    progress: Float = 0.5f,
    copyMoreToClipboard: (() -> Unit) = {},
) {
    ZeCard(
        modifier = ZeModifier
            .padding(horizontal = ZeDimen.One, vertical = ZeDimen.One)
            .background(ZeWhite, ZeRoundedCornerShape(ZeDimen.One))
            .zIndex(10.0f),
        colors = CardDefaults.cardColors(
            containerColor = ZeWhite,
            contentColor = ZeBlack,
        ),
    ) {
        ZeRow(
            modifier = ZeModifier.padding(horizontal = ZeDimen.Two, vertical = ZeDimen.One),
            verticalAlignment = ZeAlignment.CenterVertically,
        ) {
            ZeText(
                modifier = ZeModifier.weight(1.0f),
                fontSize = 20.sp,
                fontFamily = FontFamily.Monospace,
                color = ZeBlack,
                text = message,
            )

            ZeIconButton(onClick = copyMoreToClipboard) {
                ZeIcon(
                    painter = painterResource(
                        id = R.drawable.copy_clipboard,
                    ),
                    contentDescription = "Copy info bar message",
                )
            }
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = ZeModifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 0.dp, bottom = 4.dp),
            color = ZeBlack,
            trackColor = ZeWhite,
            strokeCap = StrokeCap.Round,
        )
    }
}

@Composable
@Preview
private fun BadgeConfigEditor(
    config: Map<String, Any?> = mapOf(
        stringResource(id = R.string.ze_sample_configuration_key) to stringResource(id = R.string.ze_sample_configuration_value),
        stringResource(id = R.string.ze_sample_int_key) to 23,
        stringResource(id = R.string.ze_sample_another_configuration_key) to true,
    ),
    onDismissRequest: () -> Unit = {},
    onConfirmed: (updateConfig: Map<String, Any?>) -> Unit = {},
) {
    var configState by remember { mutableStateOf(config) }
    var error by remember { mutableStateOf(mapOf<String, String>()) }

    AlertDialog(
        modifier = Modifier.imePadding(),
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(
                onClick = {
                    onConfirmed(configState)
                },
            ) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        },
        title = {
            Text(
                color = ZeBlack,
                text = stringResource(R.string.badge_config_editor_title),
            )
        },
        properties = DialogProperties(decorFitsSystemWindows = false),
        shape = AlertDialogDefaults.shape,
        containerColor = ZeWhite,
        text = {
            LazyColumn {
                items(config.keys.toList()) { key ->
                    when (val value = configState[key]) {
                        is Boolean -> AutoSizeTextField(
                            value = value.toString(),
                            label = { Text("$key (read only)") },
                            onValueChange = { },
                            placeholder = {},
                            trailingIcon = {
                                Icon(
                                    Icons.Rounded.Lock,
                                    tint = ZeBlack,
                                    contentDescription = null,
                                )
                            },
                            supportingText = { },
                        )

                        else -> AutoSizeTextField(
                            value = "$value",
                            isError = !error[key].isNullOrEmpty(),
                            onValueChange = { updated ->
                                if (updated != value) {
                                    error = error.copy(key to "")
                                    configState = configState.copy(
                                        key to updated,
                                    )
                                }
                            },
                            label = { Text(text = key) },
                            supportingText = {
                                Text(text = error.getOrDefault(key, ""))
                            },
                            trailingIcon = {},
                            placeholder = {},
                        )

                    }
                }
            }
        },
    )
}

@Composable
private fun SelectedEditor(
    editor: ZeEditor,
    vm: ZeBadgeViewModel,
) {
    if (editor.slot !in listOf(
            ZeSlot.Name,
            ZeSlot.FirstCustom,
            ZeSlot.SecondCustom,
            ZeSlot.QRCode,
            ZeSlot.Weather,
            ZeSlot.Quote,
            ZeSlot.BarCode,
        )
    ) {
        Timber.e("Slot", "This slot '${editor.slot}' is not supposed to be editable.")
    } else {
        when (val config = editor.config) {
            is ZeConfiguration.Name -> NameEditorDialog(
                config = config,
                dismissed = { vm.slotConfigured(editor.slot, null) },
                accepted = { newConfig -> vm.slotConfigured(editor.slot, newConfig) },
                updateMessage = vm::showMessage,
            )

            is ZeConfiguration.Picture -> PictureEditorDialog(
                dismissed = { vm.slotConfigured(null, null) },
                accepted = { newConfig -> vm.slotConfigured(editor.slot, newConfig) },
                updateMessage = vm::showMessage,
            )

            is ZeConfiguration.ImageGen -> ImageGenerationEditorDialog(
                initialPrompt = config.prompt,
                dismissed = { vm.slotConfigured(null, null) },
                accepted = { vm.slotConfigured(editor.slot, it) },
            )

            is ZeConfiguration.Schedule -> {
                vm.showMessage(message = stringResource(id = R.string.ze_not_added_yet_message))
                vm.slotConfigured(null, null)
            }

            is ZeConfiguration.Weather -> WeatherEditorDialog(
                config = config,
                dismissed = { vm.slotConfigured(null, null) },
                accepted = { newConfig -> vm.slotConfigured(editor.slot, newConfig) },
                updateMessage = vm::showMessage,
            )

            is ZeConfiguration.Quote -> RandomQuotesEditorDialog(
                accepted = { vm.slotConfigured(editor.slot, it) },
                dismissed = { vm.slotConfigured(null, null) },
                config = config,
                updateMessage = vm::showMessage,
            )

            is ZeConfiguration.QRCode -> QRCodeEditorDialog(
                config = config,
                dismissed = { vm.slotConfigured(editor.slot, null) },
                updateMessage = vm::showMessage,
                accepted = { newConfig -> vm.slotConfigured(editor.slot, newConfig) },
            )

            is ZeConfiguration.BarCode -> BarCodeEditorDialog(
                config = config,
                dismissed = { vm.slotConfigured(editor.slot, null) },
                accepted = { newConfig -> vm.slotConfigured(editor.slot, newConfig) },
            )

            is ZeConfiguration.Kodee ->
                vm.slotConfigured(editor.slot, config)

            is ZeConfiguration.ImageDraw -> ZeImageDrawEditorDialog(
                dismissed = { vm.slotConfigured(editor.slot, null) },
                accepted = { newConfig -> vm.slotConfigured(editor.slot, newConfig) },
            )

            is ZeConfiguration.Camera -> ZeCameraEditor(
                editor = editor,
                config = config,
                vm = vm,
            )

            is ZeConfiguration.CustomPhrase -> CustomPhraseEditorDialog(
                config = config,
                dismissed = { vm.slotConfigured(editor.slot, null) },
                udpateMessage = vm::showMessage,
                accepted = { newConfig -> vm.slotConfigured(editor.slot, newConfig) },
            )
        }
    }
}

@Composable
private fun TemplateChooserDialog(
    vm: ZeBadgeViewModel,
    templateChooser: ZeTemplateChooser?,
) {
    ZeAlertDialog(
        containerColor = ZeWhite,
        onDismissRequest = {
            vm.templateSelected(null, null)
        },
        confirmButton = {
            ZeButton(onClick = { vm.templateSelected(null, null) }) {
                ZeText(text = stringResource(id = android.R.string.ok))
            }
        },
        title = {
            ZeText(
                color = ZeBlack,
                text = stringResource(id = R.string.ze_select_content),
            )
        },
        text = {
            ZeLazyColumn {
                items(templateChooser?.configurations.orEmpty()) { config ->
                    ZeButton(
                        modifier = ZeModifier.fillMaxWidth(),
                        onClick = {
                            vm.templateSelected(templateChooser?.slot, config)
                        },
                    ) {
                        ZeText(text = config.humanTitle)
                    }
                }
            }
        },
    )
}

@Composable
@Suppress("LongParameterList")
private fun PagePreview(
    @PreviewParameter(BinaryBitmapPageProvider::class, 1)
    bitmap: Bitmap,
    modifier: ZeModifier = Modifier,
    name: String = "",
    customizeThisPage: (() -> Unit)? = null,
    resetThisPage: (() -> Unit)? = null,
    sendToDevice: (() -> Unit)? = null,
) {
    ZeCard(
        modifier = modifier
            .padding(ZeDimen.Quarter),
        colors = CardDefaults.cardColors(
            containerColor = ZeBlack,
            contentColor = ZeWhite,
        ),
        border = BorderStroke(1.dp, ZeWhite),
    ) {
        ZeImage(
            modifier = ZeModifier
                .fillMaxWidth()
                .wrapContentHeight(unbounded = true)
                .padding(horizontal = ZeDimen.One, vertical = ZeDimen.One)
                .clip(RoundedCornerShape(4.dp)),
            painter = ZeBitmapPainter(
                image = bitmap.asImageBitmap(),
                filterQuality = ZeFilterQuality.None,
            ),
            contentScale = ZeContentScale.FillWidth,
            contentDescription = null,
        )

        ZeRow {
            ZeText(
                text = name,
                modifier = Modifier
                    .align(ZeAlignment.CenterVertically)
                    .padding(start = ZeDimen.One),
            )
            if (resetThisPage != null || customizeThisPage != null || sendToDevice != null) {
                ZeLazyRow(
                    modifier = ZeModifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = ZeDimen.Quarter),
                    horizontalArrangement = ZeArrangement.End,
                ) {
                    if (sendToDevice != null) {
                        item {
                            ZeToolButton(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                text = stringResource(id = R.string.send),
                                onClick = sendToDevice,
                            )
                        }
                    }
                    if (resetThisPage != null) {
                        item {
                            ZeToolButton(
                                imageVector = Icons.Filled.Refresh,
                                text = stringResource(id = R.string.reset),
                                onClick = resetThisPage,
                            )
                        }
                    }
                    if (customizeThisPage != null) {
                        item {
                            ZeToolButton(
                                imageVector = Icons.Filled.Edit,
                                text = stringResource(id = R.string.ze_edit),
                                onClick = customizeThisPage,
                            )
                        }
                    }
                }
            }
        }
    }
}

private val ZeSlot.isSponsor: Boolean
    get() = this is ZeSlot.FirstSponsor
