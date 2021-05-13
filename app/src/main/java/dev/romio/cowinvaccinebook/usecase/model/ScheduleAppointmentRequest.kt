package dev.romio.cowinvaccinebook.usecase.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ScheduleAppointmentRequest(
    @Json(name = "beneficiaries")
    val beneficiaries: List<String>,
    @Json(name = "dose")
    val dose: Int,
    @Json(name = "center_id")
    val centerId: Int,
    @Json(name = "session_id")
    val sessionId: String,
    @Json(name = "slot")
    val slot: String,
    @Json(name = "captcha")
    val captcha: String
)