package com.lexawall.feature.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lexawall.data.local.DatabaseModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ThemePickerViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val db = DatabaseModule.getInstance(appContext)

    private val _currentXp = MutableStateFlow(0)
    val currentXp: StateFlow<Int> = _currentXp.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _currentXp.value = runCatching { db.levelDao().getLevel().currentXp }.getOrElse { 0 }
        }
    }
}
