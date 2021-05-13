package dev.romio.cowinvaccinebook.repository

import com.squareup.moshi.Moshi
import dev.romio.cowinvaccinebook.constant.AppConstant
import dev.romio.cowinvaccinebook.data.model.*
import dev.romio.cowinvaccinebook.data.network.CowinApiService
import dev.romio.cowinvaccinebook.data.preference.AppPreference
import dev.romio.cowinvaccinebook.usecase.model.ScheduleAppointmentRequest
import dev.romio.cowinvaccinebook.usecase.model.UserPreference
import dev.romio.cowinvaccinebook.util.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CowinRepositoryImpl @Inject constructor(
    private val cowinApiService: CowinApiService,
    private val preference: AppPreference,
    private val moshi: Moshi
): CowinAppRepository {

    override suspend fun generateOtp(mobile: String): ApiResult<RequestOtpResponse> {
        return safeApiCall {
            cowinApiService.requestOtp(RequestOTPModel(mobile, AppConstant.SECRET))
        }
    }

    override suspend fun generateBearerToken(
        otp: String,
        txnId: String
    ): ApiResult<ValidateOtpResponse> {
        return safeApiCall {
            cowinApiService.validateOtp(ValidateOtpModel(otp, txnId))
        }
    }

    override suspend fun fetchBeneficiaryDetails(): ApiResult<BeneficiariesResponse> {
        return safeApiCall {
            cowinApiService.getBeneficiaries()
        }
    }

    override fun saveBeneficiaryDetails(beneficiaryDetails: List<BeneficiarySummary>) {
        preference.saveBeneficiaryDetails(beneficiaryDetails)
    }

    override suspend fun getSavedBeneficiaryDetails(): List<BeneficiarySummary> = withContext(Dispatchers.IO) {
        preference.getSavedBeneficiaryDetails()
    }

    override fun observeBeneficiarySummary(): Flow<List<BeneficiarySummary>> {
        preference.getSavedBeneficiaryDetails()
        return preference.onBeneficiaryUpdated
    }

    override fun saveBearerToken(token: String) {
        preference.saveBearerToken(token)
    }

    override fun saveBearerTokenExpTime(expTime: Long) {
        preference.saveBearerTokenExpTime(expTime)
    }

    override fun saveOtp(otp: String) {
        preference.saveOtp(otp)
    }

    override fun saveOtpTxnId(txnId: String) {
        preference.saveOtpTxnId(txnId)
    }

    override fun onOtpReceived(): Flow<String> {
        return preference.onOtpSaved
    }

    override suspend fun getSavedOtp(): String? = withContext(Dispatchers.IO) {
        preference.getSavedOtp()
    }

    override suspend fun getSavedOtpTxnId(): String? = withContext(Dispatchers.IO) {
        preference.getSavedOtpTxnId()
    }

    override suspend fun getTokenExpiryTime(): Long = withContext(Dispatchers.IO){
        preference.getTokenExpiryTime()
    }

    override suspend fun getSavedMobileNum(): String? = withContext(Dispatchers.IO) {
        preference.getSavedMobileNum()
    }

    override fun saveMobileNum(mobileNum: String) {
        preference.saveMobileNum(mobileNum)
    }

    override suspend fun getBearerToken(): String? = withContext(Dispatchers.IO) {
        preference.getBearerToken()
    }

    override suspend fun getStates(): ApiResult<StatesResponse> {
        return safeApiCall { cowinApiService.getStates() }
    }

    override suspend fun getDistricts(input: Int): ApiResult<DistrictsResponse> {
        return safeApiCall { cowinApiService.getDistricts(input) }
    }

    override fun saveUserPreference(userPreference: UserPreference) {
        preference.saveUserPreference(userPreference)
    }

    override fun saveLastOTPRequestTime(time: Long) {
        preference.saveLastOTPRequestTime(time)
    }

    override suspend fun getLastOTPRequestTime(): Long = withContext(Dispatchers.IO) {
        preference.getLastOTPRequestTime()
    }

    override suspend fun getCalendarByDistrict(districtId: Int, date: String): ApiResult<CowinCalendarResponse> {
        return safeApiCall { cowinApiService.getCalendarByDistrict(districtId, date) }
    }

    override suspend fun getCalendarByPinCode(pincode: String, date: String): ApiResult<CowinCalendarResponse> {
        return safeApiCall { cowinApiService.getCalendarByPin(pincode, date) }
    }

    override suspend fun getUserPreference(): UserPreference? = withContext(Dispatchers.IO) {
        preference.getUserPreference()
    }

    override suspend fun generateCaptcha(): ApiResult<CaptchaResponse> {
        return safeApiCall { cowinApiService.generateCaptcha() }
    }

    override suspend fun scheduleAppointment(input: ScheduleAppointmentRequest): ApiResult<ScheduleAppointmentResponse> {
        return safeApiCall {
            cowinApiService.scheduleAppointment(input)
        }
    }

    override fun saveServiceRunning(isRunning: Boolean) {
        preference.saveServiceRunning(isRunning)
    }

    override suspend fun isServiceRunning(): Boolean = withContext(Dispatchers.IO) {
        preference.isServiceRunning()
    }

    override fun observeServiceRunningStatus(): Flow<Boolean> {
        return preference.onServiceRunning
    }

}