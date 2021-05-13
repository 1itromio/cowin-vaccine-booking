package dev.romio.cowinvaccinebook.usecase.model

import dev.romio.cowinvaccinebook.data.model.BeneficiarySummary

class UserPreferenceAndBeneficiaries(
    val userPreference: UserPreference,
    val beneficiaries: List<BeneficiarySummary>
)