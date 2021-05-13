package dev.romio.cowinvaccinebook.usecase

import dev.romio.cowinvaccinebook.repository.CowinAppRepository
import dev.romio.cowinvaccinebook.usecase.model.UserPreferenceAndBeneficiaries
import javax.inject.Inject

class SaveUserPreferenceUseCase @Inject constructor(
    private  val cowinAppRepository: CowinAppRepository
): BaseUseCase<UserPreferenceAndBeneficiaries, Unit>() {
    override suspend fun execute(input: UserPreferenceAndBeneficiaries) {
        cowinAppRepository.saveBeneficiaryDetails(input.beneficiaries)
        cowinAppRepository.saveUserPreference(input.userPreference)
    }
}