package de.berlindroid.zeapp.zeui.zelanguages

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ZeLanguagesViewModel @Inject constructor() : ViewModel() {
    fun setLocale(locale: String?) {
        // Call this on the main thread as it may require Activity.restart()
        viewModelScope.launch(Dispatchers.Main) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(locale))
        }
    }
}
