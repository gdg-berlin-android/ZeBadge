package de.berlindroid.zekompanion.desktop.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.berlindroid.zekompanion.desktop.State
import kotlin.math.max

@Composable
fun NameBadgeEditor(
    state: State,
    sendToBadge: () -> Unit,
    stateUpdated: (State) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
    ) {
        DrawNameBadge(state, 5.0f)

        Spacer(modifier = Modifier.weight(1.0f))

        Row {
            TextField(
                modifier = Modifier.weight(1.0f),
                value = (state as State.EditNameBadge).name,
                onValueChange = {
                    stateUpdated(state.copy(name = it))
                },
                label = { Text("Your name") },
            )

            TextField(
                modifier = Modifier.weight(1.0f),
                value = state.contact,
                onValueChange = {
                    stateUpdated(state.copy(contact = it))
                },
                label = { Text("Your contact info") },
            )
        }

        Row {
            TextField(
                modifier = Modifier.weight(1.0f),
                value = (state as State.EditNameBadge).nameFontSize.toString(),
                onValueChange = {
                    stateUpdated(
                        state.copy(
                            nameFontSize = it.toIntOrNull() ?: state.nameFontSize,
                        ),
                    )
                },
                label = { Text("Text Size") },
            )
        }

        ImageManipulators(sendToBadge)
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun DrawNameBadge(state: State, factor: Float = 1.0f) {
    val measurer = rememberTextMeasurer()

    val density = LocalDensity.current
    val (dpWidth, dpHeight) = with(density) { 296.toDp() * factor to 128.toDp() * factor }

    Column {
        // TODO: Replace with compose calls
        Canvas(
            modifier = Modifier.size(dpWidth, dpHeight).align(Alignment.CenterHorizontally),
        ) {
            val width = 296.0f * factor
            val height = 128.0f * factor

            val topBarHeight = 30.0f * factor
            val bottomBarHeight = 30.0f * factor

            val background = Color(255, 255, 255)
            val highlight = Color(255, 0, 0)

            // fill
            drawRect(color = background, size = Size(width, height))

            // bars
            drawRect(color = highlight, topLeft = Offset.Zero, size = Size(width, topBarHeight))
            drawRect(
                color = highlight,
                topLeft = Offset(0.0f, height - bottomBarHeight),
                size = Size(width, bottomBarHeight),
            )

            // words
            drawCenteredText(
                measurer = measurer,
                text = "My name is",
                fontSize = 8.sp * factor,
                color = Color.White,
                width = width,
                height = topBarHeight,
                x = 0.0f,
                y = 0.0f,
            )

            drawCenteredText(
                measurer = measurer,
                text = (state as State.EditNameBadge).name,
                fontSize = state.nameFontSize.sp * factor,
                color = Color.Black,
                width = width,
                height = height - topBarHeight - bottomBarHeight,
                x = 0.0f,
                y = topBarHeight,
            )

            drawCenteredText(
                measurer = measurer,
                text = state.contact,
                fontSize = 8.sp * factor,
                color = Color.White,
                width = width,
                height = bottomBarHeight,
                x = 0.0f,
                y = height - bottomBarHeight,
            )
        }
    }
}

@ExperimentalTextApi
private fun DrawScope.drawCenteredText(
    measurer: TextMeasurer,
    text: String,
    fontSize: TextUnit,
    color: Color,
    fontFamily: FontFamily = FontFamily.Serif,
    width: Float,
    height: Float,
    x: Float,
    y: Float,
) {
    val annotatedText = buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                fontSize = fontSize,
                color = color,
                fontFamily = fontFamily,
            ),
        ) {
            append(text)
        }
    }

    val layoutResult = measurer.measure(
        text = annotatedText,
        overflow = TextOverflow.Ellipsis,
        maxLines = 2,
    ).clipTo(width.toInt(), height.toInt())

    drawText(
        textLayoutResult = layoutResult,
        topLeft = Offset(
            max(x, width / 2.0f - layoutResult.size.width / 2.0f),
            y + max(0.0f, height / 2.0f - layoutResult.size.height / 2.0f),
        ),
    )

}

private fun TextLayoutResult.clipTo(width: Int, height: Int): TextLayoutResult {
    return if (size.width > width || size.height > height) {
        copy(layoutInput, IntSize(width, height))
    } else {
        this
    }
}
