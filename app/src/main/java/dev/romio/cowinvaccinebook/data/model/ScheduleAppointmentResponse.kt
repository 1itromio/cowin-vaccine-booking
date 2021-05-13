package dev.romio.cowinvaccinebook.data.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ScheduleAppointmentResponse(
    @Json(name = "appointment_confirmation_no")
    val appointmentConfirmationNo: String?
)