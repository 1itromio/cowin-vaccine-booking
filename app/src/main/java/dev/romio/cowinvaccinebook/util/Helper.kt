package dev.romio.cowinvaccinebook.util

import dev.romio.cowinvaccinebook.data.model.ApiResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

suspend inline fun <T> safeApiCall(dispatcher: CoroutineDispatcher = Dispatchers.IO,
                                   crossinline cb: suspend () -> T): ApiResult<T> {
    return withContext(dispatcher) {
        try {
            ApiResult.Success(cb.invoke())
        } catch (throwable: Throwable) {
            when(throwable) {
                is IOException -> ApiResult.NetworkError()
                is HttpException -> {
                    val code = throwable.code()
                    throwable.toErrorResponse()?.let {
                        ApiResult.GenericError(code, it)
                    } ?: ApiResult.GenericError.default()
                }
                else -> ApiResult.GenericError.default()
            }
        }
    }
}

