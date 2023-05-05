package de.berlindroid.zeapp

import android.app.Activity
import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Bundle
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.scale
import de.berlindroid.zeapp.bits.composableToBitmap
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
                    ZePages(this, paddingValues)
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
private fun ZePages(activity: Activity, paddingValues: PaddingValues) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(4.dp)
    ) {
        var image by remember {
            mutableStateOf(
                BitmapFactory.decodeResource(
                    activity.resources,
                    R.drawable.sample_badge,
                    BitmapFactory.Options()
                ).scale(PAGE_WIDTH, PAGE_HEIGHT)
            )
        }

        // TODO: LazyColumn with A+ design!
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
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                onClick = { image = image.dither() },
            ) { Text(text = "dither") }

            Button(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                onClick = { image = image.dither(thresholdOnly = true) },
            ) { Text(text = "threshold") }

            Button(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                onClick = { image = image.invert() },
            ) { Text(text = "invert") }

            Button(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                onClick = {
                    with(activity) {
                        // TODO: That should be in a VM
                        ask("What is your name?") { name ->
                            ask("How can we contact you?") { contact ->
                                composableToBitmap(
                                    activity = activity,
                                    content = { NamePage(name, contact) }
                                ) { bitmap ->
                                    image = bitmap
                                }
                            }
                        }
                    }
                },
            ) { Text(text = "customize") }

            Button(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                onClick = {
                    image = BitmapFactory.decodeResource(
                        activity.resources,
                        R.drawable.sample_badge,
                        BitmapFactory.Options()
                    ).scale(PAGE_WIDTH, PAGE_HEIGHT)
                },
            ) { Text(text = "reset to start badge") }
        }
    }
}

private fun Activity.ask(question: String, callback: (answer: String) -> Unit) {
    val editText = EditText(this)
    AlertDialog
        .Builder(this)
        .setTitle("Your input is needed")
        .setMessage(question)
        .setView(editText)
        .setPositiveButton(
            android.R.string.ok
        ) { dialog, _ ->
            dialog.dismiss()
            callback(editText.text.toString())
        }.show()
}


@Composable
@Preview
fun NamePage(
    name: String = "Jane Doe",
    contact: String = "jane.doe@berlindroid.de",
) {

    Column(
        modifier = Modifier
            .background(
                color = Color.White,
            )
            .size(
                width = with(LocalDensity.current) { PAGE_WIDTH.toDp() },
                height = with(LocalDensity.current) { PAGE_HEIGHT.toDp() },
            )
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.DarkGray),
            fontFamily = FontFamily.SansSerif,
            fontSize = 8.sp,
            textAlign = TextAlign.Center,
            color = Color.White,
            maxLines = 1,
            text = "Hello, my name is",
        )
        Spacer(modifier = Modifier.weight(1.0f))
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                modifier = Modifier
                    .weight(1.0f)
                    .height(
                        height = with(LocalDensity.current) { (PAGE_HEIGHT / 2).toDp() },
                    ),
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = null
            )

            Text(
                modifier = Modifier
                    .padding(1.dp)
                    .weight(2.0f),
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                text = name,
            )

            Image(
                modifier = Modifier
                    .weight(1.0f)
                    .height(
                        height = with(LocalDensity.current) { (PAGE_HEIGHT / 2).toDp() },
                    ),
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = null
            )

        }
        Text(
            modifier = Modifier.fillMaxWidth(),
            fontFamily = FontFamily.Monospace,
            fontSize = 4.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            text = contact,
        )
        Spacer(modifier = Modifier.weight(1.0f))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.DarkGray),
            fontFamily = FontFamily.SansSerif,
            fontSize = 3.sp,
            textAlign = TextAlign.Center,
            color = Color.White,
            maxLines = 1,
            text = "powered by berlindroid",
        )
    }
}
