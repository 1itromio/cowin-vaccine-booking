package dev.romio.cowinvaccinebook.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RequestOTPModel(
    @Json(name = "mobile")
    val mobileNum: String,
    @Json(name = "secret")
    val secret: String
)