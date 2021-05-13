package dev.romio.cowinvaccinebook.usecase

import dev.romio.cowinvaccinebook.data.model.ApiResult
import dev.romio.cowinvaccinebook.data.model.BeneficiariesResponse
import dev.romio.cowinvaccinebook.data.model.BeneficiarySummary
import dev.romio.cowinvaccinebook.repository.CowinAppRepository
import java.time.Year
import java.util.*
import javax.inject.Inject
// TODO("Check partially vaccinated case")
class FetchBeneficiaryDetailsUseCase @Inject constructor(
    private val cowinAppRepository: CowinAppRepository
): BaseUseCase<Unit, ApiResult<BeneficiariesResponse>>() {
    override suspend fun execute(input: Unit): ApiResult<BeneficiariesResponse> {
        val beneficiaryResp = cowinAppRepository.fetchBeneficiaryDetails()
        val savedBeneficiaryDetails = cowinAppRepository.getSavedBeneficiaryDetails()
        val currYear = Calendar.getInstance().get(Calendar.YEAR)
        if(beneficiaryResp is ApiResult.Success) {
            beneficiaryResp.value.beneficiaries.map { beneficiary ->
                BeneficiarySummary(
                    brId = beneficiary.beneficiaryReferenceId,
                    birthYear = beneficiary.birthYear,
                    gender = beneficiary.gender,
                    name = beneficiary.name,
                    photoIdNumber = beneficiary.photoIdNumber,
                    photoIdType = beneficiary.photoIdType,
                    vaccinationStatus = beneficiary.vaccinationStatus,
                    vaccine = beneficiary.vaccine,
                    age = beneficiary.birthYear?.let { currYear - it.toInt() } ?: 0,
                    appointmentCount = beneficiary.appointments?.size ?: 0,
                    isChecked = savedBeneficiaryDetails.find { it.brId == beneficiary.beneficiaryReferenceId }?.isChecked
                )
            }.also {
                cowinAppRepository.saveBeneficiaryDetails(it)
            }
        }
        return beneficiaryResp
    }
}