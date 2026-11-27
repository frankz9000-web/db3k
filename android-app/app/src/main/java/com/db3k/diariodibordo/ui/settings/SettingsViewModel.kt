package com.db3k.diariodibordo.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.db3k.diariodibordo.data.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * ViewModel for SettingsFragment
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    val pushEnabled: LiveData<Boolean> = flowToLiveData(userPreferences.pushEnabled)

    val pushNewPosts: LiveData<Boolean> = flowToLiveData(userPreferences.pushNewPosts)

    val pushRaceResults: LiveData<Boolean> = flowToLiveData(userPreferences.pushRaceResults)

    val pushEvents: LiveData<Boolean> = flowToLiveData(userPreferences.pushEvents)

    val pushSound: LiveData<Boolean> = flowToLiveData(userPreferences.pushSound)

    val pushVibration: LiveData<Boolean> = flowToLiveData(userPreferences.pushVibration)

    private fun <T> flowToLiveData(flow: kotlinx.coroutines.flow.Flow<T>): LiveData<T> {
        val liveData = MutableLiveData<T>()
        viewModelScope.launch {
            flow.collect { value ->
                liveData.postValue(value)
            }
        }
        return liveData
    }

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