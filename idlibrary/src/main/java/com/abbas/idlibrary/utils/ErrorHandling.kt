package com.abbas.idlibrary.utils

sealed class IDError {
    data object FailedInCapture : IDError()
    data object FailedInStoringPhotos : IDError()
    data object AuthenticationFailed : IDError()
    data object PermissionsNotProvided: IDError()
    data object BiometryIsNotActivated: IDError()
    data object NoBiometryExists: IDError()
    data object NoBiometryEnrolled: IDError()
    data object BiometryIsNotAvailable: IDError()
    data object CorruptedData: IDError()
}

sealed class IDThrowable: Exception() {
    data object SecretKeyGenerationException: IDThrowable() {
        private fun readResolve(): Any = SecretKeyGenerationException
    }

    data object SecretKeyRetrievalException: IDThrowable() {
        private fun readResolve(): Any = SecretKeyRetrievalException
    }

    data object CorruptedData: IDThrowable() {
        private fun readResolve(): Any = SecretKeyRetrievalException
    }
}