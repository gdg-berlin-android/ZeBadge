package de.berlindroid.zeapp.zealteregos.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.berlindroid.zeapp.zeservices.ZeUserApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class User(
    val uuid: String,
    val name: String,
    val pngUrl: String,
    val smallPngUrl: String,
    val description: String,
    val chatPhrase: String,
)

data class UiState(
    val users: List<User> = mutableListOf(),
    val selectedUser: User? = null,
)

class AlterEgosVm
    constructor(
        private val userApi: ZeUserApi,
    ) : ViewModel() {
        private val _uiState: MutableStateFlow<UiState> =
            MutableStateFlow(
                UiState(),
            )

        val uiState: StateFlow<UiState> = _uiState.asStateFlow()

        init {
            findAndLoadMaxUsers()
        }

        fun userClicked(uuid: String?) {
            _uiState.update { old ->
                old.copy(
                    selectedUser = old.users.firstOrNull { it.uuid == uuid },
                )
            }
        }

        fun findAndLoadMaxUsers() {
            viewModelScope.launch {
                val apiUsers = userApi.getUsers()

                _uiState.update { old ->
                    old.copy(
                        users =
                            apiUsers?.map { apiUser ->
                                User(
                                    uuid = apiUser.uuid,
                                    name = apiUser.name,
                                    pngUrl = userApi.getUserProfilePng(apiUser.uuid),
                                    smallPngUrl = userApi.getSmallUserProfilePng(apiUser.uuid),
                                    description = apiUser.description,
                                    chatPhrase = apiUser.chatPhrase,
                                )
                            } ?: emptyList(),
                    )
                }
            }
        }
    }
