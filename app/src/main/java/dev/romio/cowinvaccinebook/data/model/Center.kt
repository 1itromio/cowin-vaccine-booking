package dev.romio.cowinvaccinebook.data.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Center(
    @Json(name = "address")
    val address: String?,
    @Json(name = "block_name")
    val blockName: String?,
    @Json(name = "center_id")
    val centerId: Int,
    @Json(name = "district_name")
    val districtName: String?,
    @Json(name = "fee_type")
    val feeType: String?,
    @Json(name = "from")
    val from: String?,
    @Json(name = "lat")
    val lat: Int?,
    @Json(name = "long")
    val long: Int?,
    @Json(name = "name")
    val name: String?,
    @Json(name = "pincode")
    val pincode: Int?,
    @Json(name = "sessions")
    val sessions: List<Session>?,
    @Json(name = "state_name")
    val stateName: String?,
    @Json(name = "to")
    val to: String?
)