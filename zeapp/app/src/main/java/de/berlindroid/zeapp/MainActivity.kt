package de.berlindroid.zeapp

import android.content.Context
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.scale
import de.berlindroid.zeapp.bits.dither
import de.berlindroid.zeapp.bits.invert
import de.berlindroid.zeapp.ui.theme.ZeBadgeAppTheme


@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZeScreen()
        }
    }

    @Composable
    private fun ZeScreen() {
        ZeBadgeAppTheme(content = {
            Scaffold(
                topBar = {
                    ZeTopBar()
                },
                content = { paddingValues ->
                    ZePages(paddingValues, resources)
                }
            )
        })
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ZeTopBar() {
    TopAppBar(
        title = { Text(stringResource(id = R.string.app_name)) },
        actions = {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    Icons.Filled.Share,
                    contentDescription = null
                )
            }
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    Icons.Filled.KeyboardArrowUp,
                    contentDescription = null
                )
            }
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    contentDescription = null
                )
            }
        }
    )
}

@Composable
private fun ZePages(paddingValues: PaddingValues, resources: Resources) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(4.dp)
    ) {
        var image by remember {
            mutableStateOf(
                BitmapFactory.decodeResource(
                    resources,
                    R.drawable.sample_badge,
                    BitmapFactory.Options()
                ).scale(296, 128)
            )
        }

        Column {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(unbounded = true)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                painter = BitmapPainter(
                    image = image.asImageBitmap(),
                    filterQuality = FilterQuality.None,
                ),
                contentScale = ContentScale.FillWidth,
                contentDescription = null,
            )

            Button(
                onClick = { image = image.dither() },
            ) { Text(text = "Dither") }

            Button(
                onClick = { image = image.dither(thresholdOnly = true) },
            ) { Text(text = "Thres") }

            Button(
                onClick = { image = image.invert() },
            ) { Text(text = "invert") }

            Button(
                onClick = {
                    image = BitmapFactory.decodeResource(
                        resources,
                        R.drawable.sample_badge,
                        BitmapFactory.Options()
                    ).scale(296, 128)
                },
            ) { Text(text = "Reset") }
        }
    }
}
