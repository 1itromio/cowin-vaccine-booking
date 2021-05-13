package dev.romio.cowinvaccinebook.usecase

import dev.romio.cowinvaccinebook.data.model.UserDetails
import dev.romio.cowinvaccinebook.repository.CowinAppRepository
import javax.inject.Inject

class GetUserDetailsUseCase @Inject constructor(
    private val cowinAppRepository: CowinAppRepository
): BaseUseCase<Unit, UserDetails?>() {
    override suspend fun execute(input: Unit): UserDetails? {
        return cowinAppRepository.getSavedMobileNum()?.let {
            UserDetails(it)
        }
    }

}