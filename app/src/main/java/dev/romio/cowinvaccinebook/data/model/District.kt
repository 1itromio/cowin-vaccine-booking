package dev.romio.cowinvaccinebook.data.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class District(
    @Json(name = "district_id")
    val districtId: Int?,
    @Json(name = "district_name")
    val districtName: String?
)