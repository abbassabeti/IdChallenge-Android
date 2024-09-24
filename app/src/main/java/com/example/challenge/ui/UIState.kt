package com.example.challenge.ui

import android.graphics.Bitmap

sealed class UIState {
    data object Main: UIState()
    data class Gallery(
        val images: List<Bitmap>
    ): UIState()
}
