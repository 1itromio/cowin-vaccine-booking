package dev.romio.cowinvaccinebook.usecase

import dev.romio.cowinvaccinebook.data.model.ApiResult
import dev.romio.cowinvaccinebook.data.model.CaptchaResponse
import dev.romio.cowinvaccinebook.repository.CowinAppRepository
import javax.inject.Inject

class GetReCaptchaUseCase @Inject constructor(
    private val cowinAppRepository: CowinAppRepository
): BaseUseCase<Unit, ApiResult<CaptchaResponse>>() {
    override suspend fun execute(input: Unit): ApiResult<CaptchaResponse> {
        return cowinAppRepository.generateCaptcha()
    }
}