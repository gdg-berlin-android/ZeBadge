package de.berlindroid.zeapp.zeui.zehome

import android.R
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.berlindroid.zeapp.zemodels.ZeTemplateChooser
import de.berlindroid.zeapp.zeui.zetheme.ZeBlack
import de.berlindroid.zeapp.zeui.zetheme.ZeWhite
import de.berlindroid.zeapp.zevm.ZeBadgeViewModel

@Composable
internal fun TemplateChooserDialog(
    vm: ZeBadgeViewModel,
    templateChooser: ZeTemplateChooser?,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        modifier = modifier,
        containerColor = ZeWhite,
        onDismissRequest = {
            vm.templateSelected(null, null)
        },
        confirmButton = {
            Button(onClick = { vm.templateSelected(null, null) }) {
                Text(text = stringResource(id = R.string.ok))
            }
        },
        title = {
            Text(
                color = ZeBlack,
                text = stringResource(id = de.berlindroid.zeapp.R.string.ze_select_content),
            )
        },
        text = {
            LazyColumn {
                items(templateChooser?.configurations.orEmpty()) { config ->
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            vm.templateSelected(templateChooser?.slot, config)
                        },
                    ) {
                        Text(text = config.humanTitle)
                    }
                }
            }
        },
    )
}
