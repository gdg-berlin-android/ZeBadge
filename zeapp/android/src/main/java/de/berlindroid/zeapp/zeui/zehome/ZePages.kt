package de.berlindroid.zeapp.zeui.zehome

import android.graphics.Bitmap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.LayoutDirection
import de.berlindroid.zeapp.ZeDimen
import de.berlindroid.zeapp.zemodels.ZeConfiguration
import de.berlindroid.zeapp.zemodels.ZeSlot
import de.berlindroid.zeapp.zevm.ZeBadgeViewModel

@Composable
internal fun ZePages(
    paddingValues: PaddingValues,
    vm: ZeBadgeViewModel,
    lazyListState: LazyListState,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        val uiState by vm.uiState.collectAsState() // should be replace with 'collectAsStateWithLifecycle'
        val isKeyboardVisible by isKeyboardVisibleState()
        val keyboardController = LocalSoftwareKeyboardController.current

        val editor = uiState.currentSlotEditor
        val templateChooser = uiState.currentTemplateChooser
        val message = uiState.message
        val messageProgress = uiState.messageProgress
        val badgeConfiguration = uiState.currentBadgeConfig

        if (isKeyboardVisible && editor == null && templateChooser == null) {
            keyboardController?.hide()
        }

        if (badgeConfiguration != null) {
            BadgeConfigEditor(
                config = badgeConfiguration,
                onDismissRequest = vm::closeConfiguration,
                onConfirmed = vm::updateConfiguration,
                modifier = Modifier.padding(paddingValues),
            )
        }

        if (editor != null) {
            Box(Modifier.padding(paddingValues)) {
                SelectedEditor(editor, vm)
            }
        }

        if (templateChooser != null) {
            TemplateChooserDialog(vm, templateChooser, modifier = Modifier.padding(paddingValues))
        }

        ZePagesLazyList(
            modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
            paddingValues = paddingValues,
            lazyListState = lazyListState,
            slots = uiState.slots,
            sendPageToBadgeAndDisplay = vm::sendPageToBadgeAndDisplay,
            slotToBitmap = vm::slotToBitmap,
            customizeSlot = vm::customizeSlot,
            customizeSponsorSlot = vm::customizeSponsorSlot,
            resetSlot = vm::resetSlot,
        )

        if (message.isNotEmpty()) {
            InfoBar(
                modifier = Modifier.align(Alignment.BottomCenter),
                message = message,
                progress = messageProgress,
                copyMoreToClipboard = vm::copyInfoToClipboard,
            )
        }
    }
}

@Composable
private fun ZePagesLazyList(
    paddingValues: PaddingValues,
    lazyListState: LazyListState,
    slots: Map<ZeSlot, ZeConfiguration>,
    sendPageToBadgeAndDisplay: (ZeSlot) -> Unit,
    slotToBitmap: (ZeSlot) -> Bitmap,
    customizeSponsorSlot: (ZeSlot) -> Unit,
    customizeSlot: (ZeSlot) -> Unit,
    resetSlot: (ZeSlot) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        state = lazyListState,
        modifier = modifier,
        contentPadding =
        PaddingValues(
            start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
            end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
            bottom = paddingValues.calculateBottomPadding(),
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
                bitmap = slotToBitmap(slot),
                customizeThisPage =
                if (slot.isSponsor) {
                    { customizeSponsorSlot(slot) }
                } else {
                    { customizeSlot(slot) }
                },
                resetThisPage =
                if (slot.isSponsor) {
                    null
                } else {
                    { resetSlot(slot) }
                },
                sendToDevice = {
                    sendPageToBadgeAndDisplay(slot)
                },
            )

            Spacer(modifier = Modifier.height(ZeDimen.One))
        }
    }
}
