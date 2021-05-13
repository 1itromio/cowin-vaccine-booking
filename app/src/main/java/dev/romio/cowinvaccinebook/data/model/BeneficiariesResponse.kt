package dev.romio.cowinvaccinebook.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BeneficiariesResponse(
    @Json(name = "beneficiaries")
    val beneficiaries: List<Beneficiary>
)

@JsonClass(generateAdapter = true)
data class Beneficiary(
    @Json(name = "appointments")
    val appointments: List<Appointment>?,
    @Json(name = "beneficiary_reference_id")
    val beneficiaryReferenceId: String,
    @Json(name = "birth_year")
    val birthYear: String?,
    @Json(name = "comorbidity_ind")
    val comorbidityInd: String?,
    @Json(name = "dose1_date")
    val dose1Date: String?,
    @Json(name = "dose2_date")
    val dose2Date: String?,
    @Json(name = "gender")
    val gender: String?,
    @Json(name = "mobile_number")
    val mobileNumber: String?,
    @Json(name = "name")
    val name: String?,
    @Json(name = "photo_id_number")
    val photoIdNumber: String?,
    @Json(name = "photo_id_type")
    val photoIdType: String?,
    @Json(name = "vaccination_status")
    val vaccinationStatus: String?,
    @Json(name = "vaccine")
    val vaccine: String?
)

@JsonClass(generateAdapter = true)
data class Appointment(
    @Json(name = "address")
    val address: String?,
    @Json(name = "address_l")
    val addressL: String?,
    @Json(name = "appointment_id")
    val appointmentId: String?,
    @Json(name = "block_name")
    val blockName: String?,
    @Json(name = "block_name_l")
    val blockNameL: String?,
    @Json(name = "center_id")
    val centerId: Int?,
    @Json(name = "date")
    val date: String?,
    @Json(name = "district_name")
    val districtName: String?,
    @Json(name = "district_name_l")
    val districtNameL: String?,
    @Json(name = "dose")
    val dose: Int?,
    @Json(name = "fee_type")
    val feeType: String?,
    @Json(name = "from")
    val from: String?,
    @Json(name = "lat")
    val lat: Double?,
    @Json(name = "long")
    val long: Double?,
    @Json(name = "name")
    val name: String?,
    @Json(name = "name_l")
    val nameL: String?,
    @Json(name = "pincode")
    val pincode: String?,
    @Json(name = "session_id")
    val sessionId: String?,
    @Json(name = "slot")
    val slot: String?,
    @Json(name = "state_name")
    val stateName: String?,
    @Json(name = "state_name_l")
    val stateNameL: String?,
    @Json(name = "to")
    val to: String?
)

@JsonClass(generateAdapter = true)
data class BeneficiarySummary constructor(
    @Json(name = "br_id")
    val brId: String,
    @Json(name = "birth_year")
    val birthYear: String?,
    @Json(name = "gender")
    val gender: String?,
    @Json(name = "name")
    val name: String?,
    @Json(name = "photo_id_number")
    val photoIdNumber: String?,
    @Json(name = "photo_id_type")
    val photoIdType: String?,
    @Json(name = "vaccination_status")
    val vaccinationStatus: String?,
    @Json(name = "vaccine")
    val vaccine: String?,
    @Json(name = "age")
    val age: Int,
    @Json(name = "appointment_count")
    val appointmentCount: Int = 0,
    @Json(name = "is_checked")
    var isChecked: Boolean?
)