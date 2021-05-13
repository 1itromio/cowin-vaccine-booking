package dev.romio.cowinvaccinebook.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ValidateOtpModel(
    @Json(name = "otp")
    val otp: String,
    @Json(name = "txnId")
    val txnId: String
)