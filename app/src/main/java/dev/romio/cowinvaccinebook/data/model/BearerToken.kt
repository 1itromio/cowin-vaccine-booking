package dev.romio.cowinvaccinebook.data.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BearerToken(
    @Json(name = "beneficiary_reference_id")
    val beneficiaryReferenceId: Long?,
    @Json(name = "date_modified")
    val dateModified: String?,
    @Json(name = "exp")
    val exp: Long?,
    @Json(name = "iat")
    val iat: Long?,
    @Json(name = "mobile_number")
    val mobileNumber: Long?,
    @Json(name = "secret_key")
    val secretKey: String?,
    @Json(name = "ua")
    val ua: String?,
    @Json(name = "user_id")
    val userId: String?,
    @Json(name = "user_name")
    val userName: String?,
    @Json(name = "user_type")
    val userType: String?
)