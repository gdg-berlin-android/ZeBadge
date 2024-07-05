package de.berlindroid.zeapp.zeui.snackbar

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult

sealed class SnackBarData {

    data class SnackBarWithMessage(
        val message: String,
        val snackbarDuration: SnackbarDuration,
        val onDismissed: (() -> Unit)? = null,
    ) : SnackBarData()

    data class SnackBarWithAction(
        val message: String,
        val actionText: String,
        val onActionClicked: () -> Unit,
        val onDismissed: (() -> Unit)? = null,
        val snackbarDuration: SnackbarDuration,
    ) : SnackBarData()

}


suspend fun SnackbarHostState.showSnackbarWithMessage(snackBarWithMessage: SnackBarData.SnackBarWithMessage): SnackbarResult {
    return showSnackbar(
        message = snackBarWithMessage.message,
        withDismissAction = true,
        duration = snackBarWithMessage.snackbarDuration,
    )
}

suspend fun SnackbarHostState.showSnackbarWithAction(snackBarWithMessage: SnackBarData.SnackBarWithAction): SnackbarResult {
    return showSnackbar(
        message = snackBarWithMessage.message,
        withDismissAction = true,
        actionLabel = snackBarWithMessage.actionText,
        duration = snackBarWithMessage.snackbarDuration,
    )
}
