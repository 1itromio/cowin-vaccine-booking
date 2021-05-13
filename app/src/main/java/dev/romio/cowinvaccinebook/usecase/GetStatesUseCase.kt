package dev.romio.cowinvaccinebook.usecase

import dev.romio.cowinvaccinebook.data.model.ApiResult
import dev.romio.cowinvaccinebook.data.model.StatesResponse
import dev.romio.cowinvaccinebook.repository.CowinAppRepository
import javax.inject.Inject

class GetStatesUseCase @Inject constructor(
    private val cowinAppRepository: CowinAppRepository
): BaseUseCase<Unit, ApiResult<StatesResponse>>() {
    override suspend fun execute(input: Unit): ApiResult<StatesResponse> {
        return cowinAppRepository.getStates()
    }
}