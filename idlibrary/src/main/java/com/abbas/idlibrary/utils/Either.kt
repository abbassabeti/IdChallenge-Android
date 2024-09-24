package com.abbas.idlibrary.utils

sealed class Either<out S, out F> {
    // Represents a failure, containing a value of type F
    data class Success<S>(val value: S) : Either<S, Nothing>()

    // Represents a success, containing a value of type S
    data class Failure<F>(val value: F) : Either<Nothing, F>()

    // Helper functions to check whether it's success or failure
    fun isSuccess(): Boolean = this is Success
    fun isFailure(): Boolean = this is Failure

    // Helper functions to get the values safely
    fun getSuccessOrNull(): S? = (this as? Success)?.value
    fun getFailureOrNull(): F? = (this as? Failure)?.value
}