package de.berlindroid.zeapp.zeui.zehome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.berlindroid.zeapp.zeservices.github.ZeReleaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ZeDrawerViewModel @Inject constructor(
    private val releaseService: ZeReleaseService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        checkForNewRelease()
    }

    private fun checkForNewRelease() {
        viewModelScope.launch {
            val newReleaseVersion = releaseService.getNewRelease()
            _uiState.update {
                it.copy(
                    newReleaseVersion = newReleaseVersion,
                )
            }
        }
    }

    data class UiState(
        val newReleaseVersion: Int? = null, // Version of a new release, in case there is one
    )
}
