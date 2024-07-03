package de.berlindroid.zeapp.zeui.zeabout

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import de.berlindroid.zeapp.R
import de.berlindroid.zeapp.ZeDimen
import de.berlindroid.zekompanion.getPlatform

@Composable
fun ZeAbout(
    paddingValues: PaddingValues,
    vm: ZeAboutViewModel = hiltViewModel(),
) {
    val lines by vm.lines.collectAsState()

    val context = LocalContext.current
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(ZeDimen.Half),
    ) {
        Column {
            Text(
                text = "${lines.count()} contributors",
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 24.sp,
            )
            Text(
                text = "Running on '${getPlatform()}'.",
            )
            LazyColumn {
                items(lines) { line ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val email = line.substring(line.indexOf('<').plus(1), line.lastIndexOf('>')).trim()
                        Text(
                            text = line.substring(0, line.indexOf('<')).trim(),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 18.sp,
                        )
                        Icon(
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
