package com.abbas.idlibrary.utils

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt.PromptInfo

interface BiometricInterface {
    fun canAuthenticate(authenticators: Int): Int
    fun buildPromptInfo(title: String, subtitle: String, negativeButtonText: String): PromptInfo
}

class BiometricWrapper(
    private val biometricManager: BiometricManager
): BiometricInterface {
    override fun canAuthenticate(authenticators: Int): Int {
        return biometricManager.canAuthenticate(authenticators)
    }

    override fun buildPromptInfo(
        title: String,
        subtitle: String,
        negativeButtonText: String
    ): PromptInfo {
        return PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Authenticate to access protected content")
            .setNegativeButtonText("Cancel")
            .build()

    }
}