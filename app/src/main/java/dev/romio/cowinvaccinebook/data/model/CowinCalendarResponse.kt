package dev.romio.cowinvaccinebook.data.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CowinCalendarResponse(
    @Json(name = "centers")
    val centers: List<Center>?
)