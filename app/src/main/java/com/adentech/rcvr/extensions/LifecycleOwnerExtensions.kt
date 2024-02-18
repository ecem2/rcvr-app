package com.adentech.rcvr.extensions

import androidx.lifecycle.*
import com.adentech.rcvr.core.common.Event
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

fun <T> LifecycleOwner.observe(liveData: LiveData<T>, observer: (T) -> Unit) {
    liveData.observe(this, Observer {
        it?.let { t -> observer(t) }
    })
}

fun <T> LifecycleOwner.observeEvent(liveData: LiveData<Event<T>>, observer: (T) -> Unit) {
    liveData.observe(this, Observer {
        it.getContentIfNotHandled()?.let { t -> observer(t) }
    })
}

fun <T> LifecycleOwner.collect(sharedFlow: SharedFlow<T>, observer: (T) -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            sharedFlow.collect { t -> observer(t) }
        }
    }
}