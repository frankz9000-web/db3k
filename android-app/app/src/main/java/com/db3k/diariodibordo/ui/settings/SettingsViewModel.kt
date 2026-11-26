package com.db3k.diariodibordo.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.db3k.diariodibordo.data.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * ViewModel for SettingsFragment
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    val pushEnabled: LiveData<Boolean> = userPreferences.pushEnabled
        .distinctUntilChanged()
        .asLiveData(viewModelScope.coroutineContext)

    val pushNewPosts: LiveData<Boolean> = userPreferences.pushNewPosts
        .distinctUntilChanged()
        .asLiveData(viewModelScope.coroutineContext)

    val pushRaceResults: LiveData<Boolean> = userPreferences.pushRaceResults
        .distinctUntilChanged()
        .asLiveData(viewModelScope.coroutineContext)

    val pushEvents: LiveData<Boolean> = userPreferences.pushEvents
        .distinctUntilChanged()
        .asLiveData(viewModelScope.coroutineContext)

    val pushSound: LiveData<Boolean> = userPreferences.pushSound
        .distinctUntilChanged()
        .asLiveData(viewModelScope.coroutineContext)

    val pushVibration: LiveData<Boolean> = userPreferences.pushVibration
        .distinctUntilChanged()
        .asLiveData(viewModelScope.coroutineContext)

    fun setPushEnabled(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setPushEnabled(enabled) }
    }

    fun setPushNewPosts(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setPushNewPosts(enabled) }
    }

    fun setPushRaceResults(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setPushRaceResults(enabled) }
    }

    fun setPushEvents(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setPushEvents(enabled) }
    }

    fun setPushSound(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setPushSound(enabled) }
    }

    fun setPushVibration(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setPushVibration(enabled) }
    }
}