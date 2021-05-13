package dev.romio.cowinvaccinebook.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

sealed class ApiResult<out T> {
    data class Success<out T>(val value: T): ApiResult<T>()
    data class GenericError(val code: Int, val error: ErrorResponse): ApiResult<Nothing>(){
        companion object {
            fun default(): GenericError = GenericError(900, ErrorResponse("GENERIC_ERROR", "Something went wrong!!!"))
        }
        override fun toString(): String {
            return "Http Error Code: $code, ErrorCode: ${error.errorCode}, Error: ${error.error}"
        }
    }
    data class NetworkError(val error: String = "Please check your internet connection"): ApiResult<Nothing>()
}

@JsonClass(generateAdapter = true)
data class ErrorResponse(
    @Json(name = "errorCode")
    val errorCode: String,
    @Json(name = "error")
    val error: String
)