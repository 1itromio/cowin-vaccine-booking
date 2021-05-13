package dev.romio.cowinvaccinebook.usecase

import dev.romio.cowinvaccinebook.data.model.ApiResult
import dev.romio.cowinvaccinebook.data.model.ScheduleAppointmentResponse
import dev.romio.cowinvaccinebook.repository.CowinAppRepository
import dev.romio.cowinvaccinebook.usecase.model.ScheduleAppointmentRequest
import javax.inject.Inject


class ScheduleAppointmentUseCase @Inject constructor(
    private val cowinAppRepository: CowinAppRepository
): BaseUseCase<ScheduleAppointmentRequest, ApiResult<ScheduleAppointmentResponse>>() {
    override suspend fun execute(input: ScheduleAppointmentRequest): ApiResult<ScheduleAppointmentResponse> {
        return cowinAppRepository.scheduleAppointment(input)
    }
}