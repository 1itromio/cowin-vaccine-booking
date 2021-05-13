package dev.romio.cowinvaccinebook.usecase

import dev.romio.cowinvaccinebook.data.model.ApiResult
import dev.romio.cowinvaccinebook.data.model.CowinCalendarResponse
import dev.romio.cowinvaccinebook.repository.CowinAppRepository
import dev.romio.cowinvaccinebook.usecase.model.CowinCalendarRequest
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class FetchVaccinationCentersUseCase @Inject constructor(
    private val cowinAppRepository: CowinAppRepository
): BaseUseCase<CowinCalendarRequest, ApiResult<CowinCalendarResponse>>() {
    override suspend fun execute(input: CowinCalendarRequest): ApiResult<CowinCalendarResponse> {
        val date = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        return  when(input) {
            is CowinCalendarRequest.DistrictCowinCalendarRequest ->
                cowinAppRepository.getCalendarByDistrict(input.districtId, date)
            is CowinCalendarRequest.PinCowinCalendarRequest ->
                cowinAppRepository.getCalendarByPinCode(input.pincode, date)
        }
    }
}