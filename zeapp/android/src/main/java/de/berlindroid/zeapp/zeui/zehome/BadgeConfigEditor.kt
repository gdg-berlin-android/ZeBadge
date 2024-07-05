package de.berlindroid.zeapp.zeui.zehome

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import com.ban.autosizetextfield.AutoSizeTextField
import de.berlindroid.zeapp.R
import de.berlindroid.zeapp.zeui.zetheme.ZeBlack
import de.berlindroid.zeapp.zeui.zetheme.ZeWhite
import de.berlindroid.zeapp.zevm.copy

@Composable
@Preview
internal fun BadgeConfigEditor(
    modifier: Modifier = Modifier,
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
        modifier = modifier.imePadding(),
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
