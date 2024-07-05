package de.berlindroid.zeapp.zeui.zeabout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.berlindroid.zeapp.zeservices.ZeContributorsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ZeAboutViewModel @Inject constructor(
    private val contributorsService: ZeContributorsService,
) : ViewModel() {
    var page: Int = 1

    val _lines = MutableStateFlow(emptyList<Contributor>())
    val lines = _lines.asStateFlow()

    init {
        viewModelScope.launch {
            contributorsService.contributors(page).collect {
                _lines.emit(it)
            }
        }
    }

    fun loadNextPage() {
        page++
        viewModelScope.launch {
            contributorsService.contributors(page).collect {
                _lines.emit(lines.value + it)
            }
        }
    }
}
