package dev.romio.cowinvaccinebook.usecase

import dev.romio.cowinvaccinebook.data.model.ApiResult
import dev.romio.cowinvaccinebook.data.model.RequestOtpResponse
import dev.romio.cowinvaccinebook.repository.CowinAppRepository
import javax.inject.Inject

class GenerateOTPUseCase @Inject constructor(
    private val cowinAppRepository: CowinAppRepository
): BaseUseCase<Unit, ApiResult<RequestOtpResponse>>() {
    override suspend fun execute(input: Unit): ApiResult<RequestOtpResponse> {
        val savedMobileNum = cowinAppRepository.getSavedMobileNum()
        return savedMobileNum?.let {
            val apiResult = cowinAppRepository.generateOtp(savedMobileNum)
            if(apiResult is ApiResult.Success) {
                cowinAppRepository.saveOtpTxnId(apiResult.value.txnId)
                cowinAppRepository.saveLastOTPRequestTime(System.currentTimeMillis()/1000)
            }
            apiResult
        } ?: ApiResult.GenericError.default()
    }
}