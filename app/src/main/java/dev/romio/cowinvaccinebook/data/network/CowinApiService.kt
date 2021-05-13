package dev.romio.cowinvaccinebook.data.network

import dev.romio.cowinvaccinebook.data.model.*
import dev.romio.cowinvaccinebook.usecase.model.ScheduleAppointmentRequest
import retrofit2.http.*

interface CowinApiService {

    @POST("/api/v2/auth/generateMobileOTP")
    suspend fun requestOtp(@Body payload: RequestOTPModel): RequestOtpResponse

    @POST("/api/v2/auth/validateMobileOtp")
    suspend fun validateOtp(@Body payload: ValidateOtpModel): ValidateOtpResponse

    @GET("api/v2/appointment/beneficiaries")
    suspend fun getBeneficiaries(): BeneficiariesResponse

    @GET("api/v2/admin/location/states")
    suspend fun getStates(): StatesResponse

    @GET("api/v2/admin/location/districts/{districtId}")
    suspend fun getDistricts(@Path("districtId") districtId: Int): DistrictsResponse

    @GET("api/v2/appointment/sessions/public/calendarByDistrict")
    suspend fun getCalendarByDistrict(@Query("district_id") districtId: Int, @Query("date") date: String): CowinCalendarResponse

    @GET("/api/v2/appointment/sessions/public/calendarByPin")
    suspend fun getCalendarByPin(@Query("pincode") pincode: String, @Query("date") date: String): CowinCalendarResponse

    @POST("/api/v2/auth/getRecaptcha")
    suspend fun generateCaptcha(): CaptchaResponse

    @POST("/api/v2/appointment/schedule")
    suspend fun scheduleAppointment(@Body payload: ScheduleAppointmentRequest): ScheduleAppointmentResponse

}