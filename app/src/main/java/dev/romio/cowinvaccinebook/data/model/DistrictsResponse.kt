package dev.romio.cowinvaccinebook.data.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DistrictsResponse(
    @Json(name = "districts")
    val districts: List<District>?,
    @Json(name = "ttl")
    val ttl: Int?
)