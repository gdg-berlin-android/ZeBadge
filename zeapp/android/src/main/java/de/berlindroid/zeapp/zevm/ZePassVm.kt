package de.berlindroid.zeapp.zevm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.berlindroid.zeapp.zeservices.Message
import de.berlindroid.zeapp.zeservices.ZePassApi
import de.berlindroid.zeapp.zeservices.ZeUserApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MessageUi(
    val text: String,
    val userName: String,
    val profileUrl: String,
)

data class ZePassUiState(
    val messages: List<MessageUi> = emptyList(),
    val error: Boolean = false,
    val errorMessage: String = "",
)

@HiltViewModel
class ZePassVm @Inject constructor(
    private val passApi: ZePassApi,
    private val userApi: ZeUserApi,
) : ViewModel() {
    init {
        loadAllMessage()
    }

    private val _uiState: MutableStateFlow<ZePassUiState> = MutableStateFlow(
        ZePassUiState(),
    )

    val uiState: StateFlow<ZePassUiState> = _uiState.asStateFlow()

    fun loadAllMessage() {
        viewModelScope.launch {
            val newMessages = passApi.getAllMessages()
            _uiState.update {
                it.copy(messages = newMessages.toUi())
            }
        }
    }

    private suspend fun List<Message>.toUi() = map {
        MessageUi(
            text = it.message,
            profileUrl = userApi.getUserProfilePng(it.poster),
            userName = userApi.getOneUser(it.poster)?.name ?: "",
        )
    }
}
