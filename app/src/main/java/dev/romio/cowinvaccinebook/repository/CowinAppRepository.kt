package dev.romio.cowinvaccinebook.repository

import dev.romio.cowinvaccinebook.data.model.*
import dev.romio.cowinvaccinebook.usecase.model.ScheduleAppointmentRequest
import dev.romio.cowinvaccinebook.usecase.model.UserPreference
import kotlinx.coroutines.flow.Flow

interface CowinAppRepository {

    fun saveOtp(otp: String)

    fun saveOtpTxnId(txnId: String)

    fun onOtpReceived(): Flow<String>

    suspend fun getSavedOtp(): String?

    suspend fun getSavedOtpTxnId(): String?

    suspend fun getTokenExpiryTime(): Long

    suspend fun getSavedMobileNum(): String?

    fun saveMobileNum(mobileNum: String)

    fun saveBearerToken(token: String)

    fun saveBearerTokenExpTime(expTime: Long)

    suspend fun generateOtp(mobile: String): ApiResult<RequestOtpResponse>

    suspend fun generateBearerToken(otp: String, txnId: String): ApiResult<ValidateOtpResponse>

    suspend fun fetchBeneficiaryDetails(): ApiResult<BeneficiariesResponse>

    fun saveBeneficiaryDetails(beneficiaryDetails: List<BeneficiarySummary>)

    suspend fun getSavedBeneficiaryDetails(): List<BeneficiarySummary>

    fun observeBeneficiarySummary(): Flow<List<BeneficiarySummary>>

    suspend fun getBearerToken(): String?

    suspend fun getStates(): ApiResult<StatesResponse>

    suspend fun getDistricts(input: Int): ApiResult<DistrictsResponse>

    fun saveUserPreference(userPreference: UserPreference)

    fun saveLastOTPRequestTime(time: Long)

    suspend fun getLastOTPRequestTime(): Long

    suspend fun getCalendarByDistrict(districtId: Int, date: String): ApiResult<CowinCalendarResponse>

    suspend fun getCalendarByPinCode(pincode: String, date: String): ApiResult<CowinCalendarResponse>

    suspend fun getUserPreference(): UserPreference?

    suspend fun generateCaptcha(): ApiResult<CaptchaResponse>

    suspend fun scheduleAppointment(input: ScheduleAppointmentRequest): ApiResult<ScheduleAppointmentResponse>

    fun saveServiceRunning(isRunning: Boolean)

    suspend fun isServiceRunning(): Boolean

    fun observeServiceRunningStatus(): Flow<Boolean>

}