package de.berlindroid.zeapp.zeui.zeabout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.berlindroid.zeapp.zeservices.ZeContributorsService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ZeAboutViewModel @Inject constructor(
    contributorsService: ZeContributorsService,
):ViewModel(){

    val lines: StateFlow<List<Contributor>> = contributorsService.contributors()
        .stateIn(viewModelScope, SharingStarted.Lazily, initialValue = emptyList())
}
