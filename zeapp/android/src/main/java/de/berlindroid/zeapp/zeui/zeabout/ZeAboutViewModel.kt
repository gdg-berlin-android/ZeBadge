package de.berlindroid.zeapp.zeui.zeabout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.berlindroid.zeapp.zeservices.github.ZeContributorsService
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ZeAboutViewModel
    @Inject
    constructor(
        private val contributorsService: ZeContributorsService,
    ) : ViewModel() {
        private var page: Int = 1

        private val linesMutableState = MutableStateFlow(emptyList<Contributor>())
        val lines = linesMutableState.asStateFlow()

        init {
            viewModelScope.launch {
                contributorsService.contributors(page).collect {
                    linesMutableState.emit(it)
                }
            }
        }

        fun loadNextPage() {
            page++
            viewModelScope.launch {
                contributorsService.contributors(page).collect {
                    linesMutableState.emit(lines.value + it)
                }
            }
        }
    }
