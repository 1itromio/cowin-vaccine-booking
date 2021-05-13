package dev.romio.cowinvaccinebook.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserDetails(
    @Json(name = "mobile_num")
    val mobileNum: String
)
