package dev.romio.cowinvaccinebook.usecase

import android.util.Base64
import com.squareup.moshi.Moshi
import dev.romio.cowinvaccinebook.data.model.ApiResult
import dev.romio.cowinvaccinebook.data.model.BearerToken
import dev.romio.cowinvaccinebook.data.model.ValidateOtpResponse
import dev.romio.cowinvaccinebook.repository.CowinAppRepository
import dev.romio.cowinvaccinebook.util.decodeBase64String
import dev.romio.cowinvaccinebook.util.sha256
import okio.ByteString.Companion.decodeBase64
import timber.log.Timber
import javax.inject.Inject

class GenerateBearerTokenUseCase @Inject constructor(
    private val cowinAppRepository: CowinAppRepository,
    private val moshi: Moshi
): BaseUseCase<Unit, ApiResult<ValidateOtpResponse>>() {
    override suspend fun execute(input: Unit): ApiResult<ValidateOtpResponse> {
        val otp = cowinAppRepository.getSavedOtp()
        val otpTxnId = cowinAppRepository.getSavedOtpTxnId()
        if(otp != null && otpTxnId != null) {
            val otpSha = otp.sha256()
            val apiResp = cowinAppRepository.generateBearerToken(otpSha, otpTxnId)
            if(apiResp is ApiResult.Success) {
                val token = apiResp.value.token
                Timber.d("Bearer token: %s", token)
                cowinAppRepository.saveBearerToken(token)
                val tokenPart = kotlin.run {
                    val tokenSplit = token.split(".")
                    if(tokenSplit.size == 3) {
                        tokenSplit[1]
                    } else null
                }
                if(tokenPart != null) {
                    val jsonToken =tokenPart.decodeBase64String()
                    Timber.d("JSON Token: %s", jsonToken)
                    moshi.adapter(BearerToken::class.java).fromJson(jsonToken)?.also {
                        if(it.exp != null) {
                            cowinAppRepository.saveBearerTokenExpTime(it.exp)
                        }
                    }
                }
            }
            return apiResp
        }
        return ApiResult.GenericError.default()
    }
}