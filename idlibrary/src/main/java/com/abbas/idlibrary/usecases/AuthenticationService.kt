package com.abbas.idlibrary.usecases

import android.content.Context
import androidx.biometric.BiometricManager
import com.abbas.idlibrary.utils.BiometricInterface
import com.abbas.idlibrary.utils.BiometricPromptBuilder
import com.abbas.idlibrary.utils.Either
import com.abbas.idlibrary.utils.IDError
import javax.inject.Inject

interface AuthenticationService {
    suspend fun authenticateUser(context: Context): Either<Unit, IDError>
}

class AuthenticationServiceImpl @Inject constructor(
    private val biometricPromptBuilder: BiometricPromptBuilder,
    private val biometricWrapper: BiometricInterface
) : AuthenticationService {

    override suspend fun authenticateUser(context: Context): Either<Unit, IDError> {

        // Check if biometric authentication is available
        return when (biometricWrapper.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                // Biometric is available, proceed with authentication
                showBiometricPrompt(context)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Either.Failure(IDError.NoBiometryExists)
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Either.Failure(IDError.BiometryIsNotActivated)
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                Either.Failure(IDError.NoBiometryEnrolled)
            else ->
                Either.Failure(IDError.BiometryIsNotAvailable)
        }
    }

    private suspend fun showBiometricPrompt(context: Context): Either<Unit, IDError> {
        return biometricPromptBuilder.showPrompt(context, biometricWrapper)
    }
}