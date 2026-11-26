package com.db3k.diariodibordo.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Extension functions for LiveData and Flowable interop
 */
fun <T> Flowable<T>.asLiveData(scope: CoroutineScope = kotlinx.coroutines.GlobalScope): LiveData<T> {
    val liveData = MutableLiveData<T>()
    val disposable = subscribe(
        { liveData.postValue(it) },
        { e -> e.printStackTrace() }
    )
    // Note: In a real app, you'd want to dispose this when the LiveData is no longer observed
    return liveData
}

fun <T> Single<T>.asLiveData(scope: CoroutineScope = kotlinx.coroutines.GlobalScope): LiveData<T> {
    val liveData = MutableLiveData<T>()
    val disposable = subscribe(
        { liveData.postValue(it) },
        { e -> e.printStackTrace() }
    )
    return liveData
}

/**
 * Safe launch for ViewModel scope
 */
fun ViewModel.safeLaunch(block: suspend CoroutineScope.() -> Unit) {
    viewModelScope.launch { block() }
}