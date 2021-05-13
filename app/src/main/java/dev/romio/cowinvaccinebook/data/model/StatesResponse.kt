package dev.romio.cowinvaccinebook.data.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StatesResponse(
    @Json(name = "states")
    val states: List<State>?,
    @Json(name = "ttl")
    val ttl: Int?
)