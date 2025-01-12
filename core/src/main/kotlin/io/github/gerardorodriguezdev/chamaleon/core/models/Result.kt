package io.github.gerardorodriguezdev.chamaleon.core.models

import io.github.gerardorodriguezdev.chamaleon.core.models.Result.Failure
import io.github.gerardorodriguezdev.chamaleon.core.models.Result.Success
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

internal sealed class Result<out S, out F> {
    class Success<out S>(val value: S) : Result<S, Nothing>()
    class Failure<out F>(val value: F) : Result<Nothing, F>()

    companion object {
        fun <T> T.toSuccess(): Success<T> = Success(this)
        fun <T> T.toFailure(): Failure<T> = Failure(this)
    }
}

@OptIn(ExperimentalContracts::class)
internal fun <S, F> Result<S, F>.isFailure(): Boolean {
    contract {
        returns(true) implies (this@isFailure is Failure<F>)
        returns(false) implies (this@isFailure is Success<S>)
    }
    return this is Failure<F>
}