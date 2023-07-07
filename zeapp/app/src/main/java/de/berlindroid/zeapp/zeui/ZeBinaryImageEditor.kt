package de.berlindroid.zeapp.zeui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import de.berlindroid.zeapp.PAGE_HEIGHT
import de.berlindroid.zeapp.PAGE_WIDTH
import de.berlindroid.zeapp.R
import de.berlindroid.zeapp.ZeDimen
import de.berlindroid.zeapp.zebits.copy
import de.berlindroid.zeapp.zebits.ditherFloydSteinberg
import de.berlindroid.zeapp.zebits.ditherPositional
import de.berlindroid.zeapp.zebits.ditherStaticPattern
import de.berlindroid.zeapp.zebits.invert
import de.berlindroid.zeapp.zebits.threshold

/**
 * Embeddable editor that let's the user turn a given bitmap into a binary image
 *
 * @param original the initial bitmap to be converted
 * @param bitmapUpdated update callback to be called when the bitmap is updated.
 */
@Composable
@Preview
fun BinaryImageEditor(
    @PreviewParameter(BinaryBitmapPageProvider::class, 1)
    original: Bitmap,
    bitmapUpdated: (Bitmap) -> Unit = {},
) {
    var transformChain by remember { mutableStateOf(TransformChain()) }
    var last by remember { mutableStateOf(original) }
    last = transformChain.buildResult(original)

    val publish = {
        last = transformChain.buildResult(original)
        bitmapUpdated(last)
    }

    Column {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(unbounded = false)
                .height(PAGE_HEIGHT.dp)
                .padding(horizontal = ZeDimen.One, vertical = ZeDimen.Half),
            painter = BitmapPainter(
                image = last.asImageBitmap(),
                filterQuality = FilterQuality.None,
            ),
            contentScale = ContentScale.FillWidth,
            contentDescription = null,
        )

        Text(text = stringResource(id = R.string.section_preprocessor))

        // Right you can only have one preprocessor, but there's a foundation for chaining several
        // All that's left if a better UI for that
        PreProcessorSelector(
            canReset = transformChain.hasPreprocessors()
        ) {
            transformChain = transformChain.copy(
                preProcessors = it?.let { listOf(it) } ?: emptyList()
            )

            publish()
        }

        Text(text = stringResource(id = R.string.section_binarizer))
        BinarizerSelector(
            canReset = transformChain.hasBinarizer()
        ) {
            transformChain = transformChain.copy(
                binarizer = it
            )

            publish()
        }

    }
}

@Composable
private fun PreProcessorSelector(
    canReset: Boolean,
    onPreprocessorSelected: (BitmapTransformer?) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = ZeDimen.Quarter),
        horizontalArrangement = Arrangement.Start
    ) {
        if (canReset) {
            item {
                ToolButton(
                    painter = painterResource(id = R.drawable.binary_bitmap_modificator_undo),
                    text = stringResource(id = R.string.reset),
                    onClick = {
                        onPreprocessorSelected(null)
                    }
                )
            }
        }
        item {
            ToolButton(
                painter = painterResource(id = R.drawable.binary_bitmap_modificator_invert),
                text = stringResource(id = R.string.invert)
            ) {
                onPreprocessorSelected(Transformer { it.invert() })
            }
        }
    }
}

@Composable
private fun BinarizerSelector(
    canReset: Boolean,
    onPreprocessorSelected: (BitmapTransformer?) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = ZeDimen.Quarter),
        horizontalArrangement = Arrangement.Start
    ) {
        if (canReset) {
            item {
                ToolButton(
                    painter = painterResource(id = R.drawable.binary_bitmap_modificator_undo),
                    text = stringResource(id = R.string.reset),
                    onClick = {
                        onPreprocessorSelected(null)
                    }
                )
            }
        }
        item {
            ToolButton(
                painter = painterResource(id = R.drawable.binary_bitmap_modificator_threshold),
                text = stringResource(id = R.string.black_and_white)
            ) {
                onPreprocessorSelected(Transformer { it.threshold() })
            }
        }
        item {
            ToolButton(
                painter = painterResource(id = R.drawable.binary_bitmap_modificator_floyd_steinberg),
                text = stringResource(id = R.string.floyd_steninberg_initials)
            ) {
                onPreprocessorSelected(Transformer { it.ditherFloydSteinberg() })
            }
        }
        item {
            ToolButton(
                painter = painterResource(id = R.drawable.binary_bitmap_modificator_static),
                text = "Static"
            ) {
                onPreprocessorSelected(Transformer { it.ditherStaticPattern() })
            }
        }
        item {
            ToolButton(
                painter = painterResource(id = R.drawable.binary_bitmap_modificator_positional),
                text = stringResource(id = R.string.positional)
            ) {
                onPreprocessorSelected(Transformer { it.ditherPositional() })
            }
        }
    }
}


// TODO move from Bitmaps to domain types to distinguish between monochrome and regular bitmaps
private interface BitmapTransformer {
    fun transform(bitmap: Bitmap): Bitmap
}

private class Transformer(private val tranformation: (Bitmap) -> Bitmap): BitmapTransformer {
    override fun transform(bitmap: Bitmap): Bitmap {
        return tranformation(bitmap)
    }

}


private data class TransformChain(
    private val preProcessors: List<BitmapTransformer> = listOf(),
    private var binarizer: BitmapTransformer? = null
) {
    fun hasPreprocessors() = preProcessors.isNotEmpty()
    fun hasBinarizer() = binarizer != null

    fun buildResult(source: Bitmap): Bitmap {
        val preprocessed = preProcessors.fold(
            source
        ) { bitmap, transformation ->
            transformation.transform(bitmap)
        }

        return binarizer?.transform(preprocessed) ?: preprocessed
    }
}

class BinaryBitmapPageProvider : PreviewParameterProvider<Bitmap> {
    override val values: Sequence<Bitmap>
        get() {
            return sequenceOf(
                with(Bitmap.createBitmap(PAGE_WIDTH, PAGE_HEIGHT, Bitmap.Config.ARGB_8888)) {
                    this
                },
            )
        }
}
