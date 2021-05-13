package dev.romio.cowinvaccinebook.usecase.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserPreference(
    @Json(name = "vaccineType" )
    val vaccineType: VaccineType,
    @Json(name = "freeVaccinePref")
    val feeTypePref: FeeType,
    @Json(name = "ageGroup")
    val ageGroup: AgeGroup,
    @Json(name = "stateId")
    val stateId: Int?,
    @Json(name = "stateName")
    val stateName: String?,
    @Json(name = "districtId")
    val districtId: Int?,
    @Json(name = "districtName")
    val districtName: String?,
    @Json(name = "refreshInterval")
    val refreshInterval: Int,
    @Json(name = "districtOrPinPref")
    val districtOrPinPref: PinOrDistrictPref,
    @Json(name = "pin")
    val pin: String?
)