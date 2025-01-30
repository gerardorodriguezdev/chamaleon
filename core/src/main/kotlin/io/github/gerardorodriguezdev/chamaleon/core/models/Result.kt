package io.github.gerardorodriguezdev.chamaleon.core.models

internal sealed class Result<out S, out F> {
    class Success<out S>(val value: S) : Result<S, Nothing>()
    class Failure<out F>(val value: F) : Result<Nothing, F>()

    fun successValue(): S = (this as Success<S>).value
    fun failureValue(): F = (this as Failure<F>).value

    fun isFailure(): Boolean = this is Failure

    companion object {
        fun <T> T.toSuccess(): Success<T> = Success(this)
        fun <T> T.toFailure(): Failure<T> = Failure(this)
    }
}