package dev.romio.cowinvaccinebook.data.preference

import android.content.Context
import androidx.core.content.edit
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.romio.cowinvaccinebook.data.model.BeneficiarySummary
import dev.romio.cowinvaccinebook.usecase.model.UserPreference
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreference @Inject constructor(
    @ApplicationContext private val context: Context,
    private val moshi: Moshi
) {

    companion object {
        private const val PREF_FILE_NAME = "dev.romio.cowinvaccinebook.CowinAppPref"
        private const val KEY_OTP = "otp"
        private const val KEY_OTP_TXN_ID = "otp_txn_id"
        private const val KEY_TOKEN_EXPIRY_TIME = "token_exp_time"
        private const val KEY_SAVED_MOBILE_NUM = "saved_mobile_num"
        private const val KEY_BEARER_TOKEN = "bearer_token"
        private const val KEY_BENEFICIARY_DETAILS = "beneficiary_details"
        private const val KEY_USER_PREFERENCE = "user_preference"
        private const val KEY_LAST_OTP_REQUEST_TIME = "last_otp_request_time"
        private const val KEY_IS_SERVICE_RUNNING = "is_service_running"
    }

    private val otpChannel = BroadcastChannel<String>(1)
    private val beneficiariesChannel = BroadcastChannel<List<BeneficiarySummary>>(Channel.CONFLATED)
    private val serviceRunningChannel = BroadcastChannel<Boolean>(Channel.CONFLATED)
    val onOtpSaved: Flow<String> = otpChannel.asFlow()
    val onBeneficiaryUpdated = beneficiariesChannel.asFlow()
    val onServiceRunning = serviceRunningChannel.asFlow()

    private val preference by lazy {
        context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
    }

    fun saveOtp(otp: String) {
        preference.edit {
            putString(KEY_OTP, otp).apply()

        }
        otpChannel.offer(otp)
    }

    fun saveOtpTxnId(txnId: String) {
        preference.edit {
            putString(KEY_OTP_TXN_ID, txnId).apply()
        }
    }

    fun getTokenExpiryTime(): Long {
        return preference.getLong(KEY_TOKEN_EXPIRY_TIME, 0L)
    }

    fun saveMobileNum(mobileNum: String) {
        preference.edit {
            putString(KEY_SAVED_MOBILE_NUM, mobileNum).apply()
        }
    }

    fun getSavedMobileNum(): String? {
        return preference.getString(KEY_SAVED_MOBILE_NUM, null)
    }

    fun getSavedOtp(): String? {
        return preference.getString(KEY_OTP, null)
    }

    fun getSavedOtpTxnId(): String? {
        return preference.getString(KEY_OTP_TXN_ID, null)
    }

    fun saveBearerToken(token: String) {
        preference.edit {
            putString(KEY_BEARER_TOKEN, token).apply()
        }
    }

    fun saveBearerTokenExpTime(expTime: Long) {
        preference.edit {
            putLong(KEY_TOKEN_EXPIRY_TIME, expTime).apply()
        }
    }

    fun saveBeneficiaryDetails(beneficiaries: List<BeneficiarySummary>) {
        val beneficiaryDetails = moshi.adapter<List<BeneficiarySummary>>(
            Types.newParameterizedType(
                MutableList::class.java,
                BeneficiarySummary::class.java
            )).toJson(beneficiaries)
        preference.edit {
            putString(KEY_BENEFICIARY_DETAILS, beneficiaryDetails).apply()
        }
        Timber.d("Saving beneficiary details: %s", beneficiaryDetails)
        beneficiariesChannel.offer(beneficiaries)
    }

    fun getSavedBeneficiaryDetails(): List<BeneficiarySummary> {
        val beneficiaries =  preference.getString(KEY_BENEFICIARY_DETAILS, null)?.let {
            moshi.adapter<List<BeneficiarySummary>>(
                Types.newParameterizedType(
                    MutableList::class.java,
                    BeneficiarySummary::class.java
                )).fromJson(it)
        } ?: arrayListOf()
        beneficiariesChannel.offer(beneficiaries)
        return beneficiaries
    }

    fun getBearerToken(): String? {
        return preference.getString(KEY_BEARER_TOKEN, null)
    }

    fun saveUserPreference(userPreference: UserPreference) {
        preference.edit {
            putString(KEY_USER_PREFERENCE,
                moshi.adapter(UserPreference::class.java).toJson(userPreference))
                .apply()
        }
    }

    fun saveLastOTPRequestTime(time: Long) {
        preference.edit {
            putLong(KEY_LAST_OTP_REQUEST_TIME, time).apply()
        }
    }

    fun getLastOTPRequestTime(): Long {
        return preference.getLong(KEY_LAST_OTP_REQUEST_TIME, 0L)
    }

    fun getUserPreference(): UserPreference? {
        return preference.getString(KEY_USER_PREFERENCE, null)?.let {
            moshi.adapter(UserPreference::class.java).fromJson(it)
        }
    }

    fun saveServiceRunning(running: Boolean) {
        preference.edit {
            putBoolean(KEY_IS_SERVICE_RUNNING, running).apply()
        }
        serviceRunningChannel.offer(running)
    }

    fun isServiceRunning(): Boolean {
        val res =  preference.getBoolean(KEY_IS_SERVICE_RUNNING, false)
        serviceRunningChannel.offer(res)
        return res
    }


}