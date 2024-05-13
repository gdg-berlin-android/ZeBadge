package com.ban.autosizetextfield

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.ParagraphIntrinsics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.berlindroid.zeapp.zeui.zetheme.ZeBlack
import de.berlindroid.zeapp.zeui.zetheme.ZeWhite

private const val TEXT_SCALE_REDUCTION_INTERVAL = 0.7f

/**
 * copied from https://github.com/banmarkovic/AutoSizeTextField
 */
@Composable
fun AutoSizeTextField(
    modifier: Modifier = Modifier,
    value: String,
    fontSize: TextUnit = 24.sp,
    lineHeight: TextUnit = 36.sp,
    onValueChange: (String) -> Unit,
    supportingText: @Composable () -> Unit,
    label: @Composable () -> Unit = { },
    trailingIcon: @Composable () -> Unit,
    placeholder: @Composable () -> Unit,
    fontWeight: FontWeight = FontWeight.SemiBold,
    textAlign: TextAlign = TextAlign.Center,
    isError: Boolean = false,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        var shrunkFontSize = fontSize
        val calculateIntrinsics = @Composable {
            ParagraphIntrinsics(
                text = value,
                style = TextStyle(
                    fontSize = shrunkFontSize,
                    fontWeight = fontWeight,
                    lineHeight = lineHeight,
                ),
                density = LocalDensity.current,
                fontFamilyResolver = createFontFamilyResolver(LocalContext.current),
            )
        }

        var intrinsics = calculateIntrinsics()
        with(LocalDensity.current) {
            // TextField and OutlinedText field have default horizontal padding of 16.dp
            val textFieldDefaultHorizontalPadding = 16.dp.toPx()
            val maxInputWidth = maxWidth.toPx() - 2 * textFieldDefaultHorizontalPadding

            while (intrinsics.maxIntrinsicWidth > maxInputWidth) {
                shrunkFontSize *= TEXT_SCALE_REDUCTION_INTERVAL
                intrinsics = calculateIntrinsics()
            }
        }

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = { onValueChange(it) },
            textStyle = TextStyle(
                fontSize = shrunkFontSize,
                fontWeight = fontWeight,
                lineHeight = lineHeight,
                textAlign = textAlign,
            ),
            label = label,
            singleLine = true,
            supportingText = supportingText,
            trailingIcon = trailingIcon,
            placeholder = placeholder,
            isError = isError,
            colors = OutlinedTextFieldDefaults.colors().copy(
                focusedContainerColor = ZeBlack,
                focusedTextColor = ZeWhite,
                focusedLabelColor = ZeBlack,
                unfocusedContainerColor = ZeWhite,
                unfocusedTextColor = ZeBlack,
                unfocusedLabelColor = ZeBlack,
                cursorColor = ZeWhite,
                errorContainerColor = Color.Red,
                errorLabelColor = Color.Red,
            ),
        )
    }
}
