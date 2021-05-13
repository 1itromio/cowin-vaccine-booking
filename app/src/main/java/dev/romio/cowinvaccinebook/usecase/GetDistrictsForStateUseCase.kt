package dev.romio.cowinvaccinebook.usecase

import dev.romio.cowinvaccinebook.data.model.ApiResult
import dev.romio.cowinvaccinebook.data.model.DistrictsResponse
import dev.romio.cowinvaccinebook.repository.CowinAppRepository
import javax.inject.Inject

class GetDistrictsForStateUseCase @Inject constructor(
    private val cowinAppRepository: CowinAppRepository
): BaseUseCase<Int, ApiResult<DistrictsResponse>>() {
    override suspend fun execute(input: Int): ApiResult<DistrictsResponse> {
        return cowinAppRepository.getDistricts(input)
    }
}