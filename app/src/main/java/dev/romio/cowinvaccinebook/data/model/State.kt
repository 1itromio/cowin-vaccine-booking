package dev.romio.cowinvaccinebook.data.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class State(
    @Json(name = "state_id")
    val stateId: Int?,
    @Json(name = "state_name")
    val stateName: String?
)