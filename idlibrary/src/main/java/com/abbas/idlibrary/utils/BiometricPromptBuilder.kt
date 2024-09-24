package com.abbas.idlibrary.utils

import android.content.Context
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

interface BiometricPromptBuilder {
    suspend fun showPrompt(
        context: Context,
        biometricWrapper: BiometricInterface
    ): Either<Unit, IDError>
}

class BiometricPromptBuilderImpl : BiometricPromptBuilder {
    override suspend fun showPrompt(
        context: Context,
        biometricWrapper: BiometricInterface
    ): Either<Unit, IDError> {
        val executor = ContextCompat.getMainExecutor(context)
        return suspendCancellableCoroutine { continuation ->
            // Define a BiometricPrompt using the builder
            val biometricPrompt = buildBiometricPrompt(
                context as FragmentActivity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        continuation.resume(Either.Failure(IDError.AuthenticationFailed))
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        continuation.resume(Either.Success(Unit))
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        continuation.resume(Either.Failure(IDError.AuthenticationFailed))
                    }
                }
            )

            // Set up the prompt dialog
            val promptInfo = biometricWrapper.buildPromptInfo(
                title = "Biometric Authentication",
                subtitle = "Authenticate to access protected content",
                negativeButtonText = "Cancel"
            )

            // Show the biometric prompt to the user
            biometricPrompt.authenticate(promptInfo)
        }
    }

    private fun buildBiometricPrompt(
        activity: FragmentActivity,
        executor: java.util.concurrent.Executor,
        callback: BiometricPrompt.AuthenticationCallback
    ): BiometricPrompt {
        return BiometricPrompt(activity, executor, callback)
    }
}