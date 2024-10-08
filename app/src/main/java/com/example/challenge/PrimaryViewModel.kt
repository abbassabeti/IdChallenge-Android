package com.example.challenge

import android.app.Activity
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abbas.idlibrary.IdVerification
import com.abbas.idlibrary.utils.Either
import com.example.challenge.ui.UIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrimaryViewModel @Inject constructor() : ViewModel() {
    var idVerification: IdVerification? = null
    val _uiState = mutableStateOf<UIState>(value = UIState.Main)
    val uiState: State<UIState> = _uiState
    fun takePhoto() {
        viewModelScope.launch {

            when (val result = idVerification?.takePhoto()) {
                is Either.Success<*> -> {
                    // Handle success
                    println("Photo captured and stored successfully")
                }
                is Either.Failure -> {
                    println("Failed to capture photo: ${result.value}")
                }
                else -> {
                    // Handle failure
                    println("Something went wrong")
                }
            }
        }
    }

    // Call the authenticateUser method in IdVerification
    fun authenticateUser(activity: Activity) {
        viewModelScope.launch {
            when (val result = idVerification?.authenticateUser(activity)) {
                is Either.Success -> {
                    // Handle success
                    println("User authenticated successfully")
                }
                is Either.Failure -> {
                    // Handle failure
                    println("Authentication failed: ${result.value}")
                }
                else -> {
                    println("Unknown Error")
                }
            }
        }
    }

    // Call the accessPhotos method in IdVerification
    fun accessPhotos(activity: Activity) {
        viewModelScope.launch {
            when (val result = idVerification?.accessPhotos(activity)) {
                is Either.Success -> {
                    val photos = result.value
                    _uiState.value = UIState.Gallery(photos)
                }

                is Either.Failure -> {
                    println("Access photos failed: ${result.value}")
                }
                else -> {
                    println("Unknown Error")
                }
            }
        }
    }

    fun onBack() {
        _uiState.value = UIState.Main
    }
}