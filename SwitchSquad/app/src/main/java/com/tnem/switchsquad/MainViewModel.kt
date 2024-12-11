package com.tnem.switchsquad

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.Executors;

class MainViewModel : ViewModel() {
    private val _socketStatus = MutableLiveData(false)
    var socketStatus: LiveData<Boolean> = _socketStatus

    //    private MutableLiveData<Pair<Boolean, String>> _message = new MutableLiveData<>(new Pair<>(false, ""));
    //    LiveData<Pair<Boolean, String>> message = _message;
    private val _message = MutableLiveData("")
    var message: LiveData<String> = _message

    fun setStatus(status: Boolean) {
        // Use a suitable executor for main thread operations
        // Note: Avoid using GlobalScope.launch directly in Android as it can lead to memory leaks
        // Consider using Android-specific mechanisms for main thread execution
        // (e.g., viewModelScope, lifecycleScope, or Handler)
        Executors.newSingleThreadExecutor().execute {
            // Access and update _socketStatus on the main thread
            _socketStatus.postValue(status)
        }
    }

    fun setMessage(message: String) {
        // Use a suitable executor for main thread operations
        // Consider Android-specific mechanisms if applicable
        Executors.newSingleThreadExecutor().execute {
            // Access and potentially update _message on the main thread
            _message.postValue(message) // Use postValue for thread-safety
        }
    }
}