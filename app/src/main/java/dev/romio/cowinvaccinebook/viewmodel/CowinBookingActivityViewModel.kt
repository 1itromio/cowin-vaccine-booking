package dev.romio.cowinvaccinebook.viewmodel

import android.app.Application
import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.romio.cowinvaccinebook.R
import dev.romio.cowinvaccinebook.data.model.ApiResult
import dev.romio.cowinvaccinebook.data.model.BeneficiarySummary
import dev.romio.cowinvaccinebook.data.model.Center
import dev.romio.cowinvaccinebook.data.model.Session
import dev.romio.cowinvaccinebook.repository.CowinAppRepository
import dev.romio.cowinvaccinebook.usecase.GetReCaptchaUseCase
import dev.romio.cowinvaccinebook.usecase.ScheduleAppointmentUseCase
import dev.romio.cowinvaccinebook.usecase.model.ScheduleAppointmentRequest
import dev.romio.cowinvaccinebook.util.ResourceProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CowinBookingActivityViewModel @Inject constructor(
    private val getReCaptchaUseCase: GetReCaptchaUseCase,
    private val scheduleAppointmentUseCase: ScheduleAppointmentUseCase,
    private val cowinAppRepository: CowinAppRepository,
    private val resourceProvider: ResourceProvider,
): BaseViewModel() {

    // TODO Retry captchas

    private val captchaLineReplaceRegex by lazy {
        "(<path d=)(.*?)(fill=\"none\"/>)".toRegex()
    }

    var centers: List<Center>? = null
        private set
    private var centerIdToSessionsMap: Map<Int, List<Session>>? = null

    var currCenterCount = 0
        private set
    var currSessionCountForCenter = 0
        private set

    private val _currCenter = MutableLiveData<Center>()
    private val _currSession = MutableLiveData<Session>()
    private val _currCaptcha = MutableLiveData<String>()
    private val _appointmentScheduleSuccess = MutableLiveData<String>()
    private val _appointmentScheduleFailed = MutableLiveData<String>()
    private val _finishBooking = MutableLiveData<Boolean>()
    private val _fetchingCaptcha = MutableLiveData<Boolean>()
    private val _scheduleAppointment = MutableLiveData<Boolean>()

    val currCenter: LiveData<Center> = _currCenter
    val currSession: LiveData<Session> = _currSession
    val currCaptcha: LiveData<String> = _currCaptcha
    val onAppointmentScheduleSuccess: LiveData<String> = _appointmentScheduleSuccess
    val onAppointmentScheduleFailed: LiveData<String> = _appointmentScheduleFailed
    val finishBooking: LiveData<Boolean> = _finishBooking
    val onFetchingCaptcha: LiveData<Boolean> = _fetchingCaptcha
    val onScheduleAppointment: LiveData<Boolean> = _scheduleAppointment

    //private var currSelectedSession: Session? = null
    //private var currSelectedCenter: Center? = null

    var beneficiaries: List<BeneficiarySummary>? = null

    fun initiateBookingProcedure(
        centers: List<Center>?,
        centerIdToSessionsMap: MutableMap<Int, List<Session>>?
    ) {
        this.centers = centers
        this.centerIdToSessionsMap = centerIdToSessionsMap
        viewModelScope.launch {
            fetchCaptcha()
        }
        book()
    }

    fun book() {
        if((centers?.size?: 0) > currCenterCount) {
            var currCenter = centers?.get(currCenterCount)
            val sessionsForCenter = centerIdToSessionsMap?.get(currCenter?.centerId)
            val currSession = if(currSessionCountForCenter >= (sessionsForCenter?.size ?: 0)) {
                currCenterCount += 1
                currCenter = if(currCenterCount >= (centers?.size ?: 0)) null else centers?.get(currCenterCount)
                currSessionCountForCenter = 0
                centerIdToSessionsMap?.get(currCenter?.centerId)?.get(currSessionCountForCenter)
            } else {
                sessionsForCenter?.get(currSessionCountForCenter)
            }
            viewModelScope.launch {
                if(currCenter != null && currSession != null) {
                    _currCenter.postValue(currCenter)
                    _currSession.postValue(currSession)
                } else {
                    Timber.d("Finishing booking at 74")
                    _finishBooking.postValue(true)
                }
            }
        } else {
            Timber.d("Finishing booking at 79")
            _finishBooking.postValue(true)
        }
    }

    private suspend fun fetchCaptcha() {
        _fetchingCaptcha.postValue(true)
        val captcha = getReCaptcha()
        _fetchingCaptcha.postValue(false)
        if(captcha != null) {
            val cleanCaptcha = captcha.replace(captchaLineReplaceRegex, "")
            val base64Captcha = Base64.encodeToString(cleanCaptcha.toByteArray(), Base64.DEFAULT)
            _currCaptcha.postValue(getHTMLBody(base64Captcha))
        } else {
            _appointmentScheduleFailed.postValue(resourceProvider.getString(R.string.getting_inavlid_captcha))
        }
    }

    fun bookNextCenter() {
        currCenterCount++
        book()
    }

    fun bookNextSession() {
        currSessionCountForCenter++
        book()
    }

    fun scheduleAppointment(captcha: String, slot: String) {
        viewModelScope.launch {
            _scheduleAppointment.postValue(true)
            if(beneficiaries == null) {
                beneficiaries = cowinAppRepository.getSavedBeneficiaryDetails().filter { it.isChecked == true }
            }
            val currSession = _currSession.value
            val currCenter = _currCenter.value
            if(currSession!= null && currCenter != null) {
                val reqModel = ScheduleAppointmentRequest(
                    beneficiaries = beneficiaries!!.map { it.brId },
                    dose = 1,
                    sessionId = currSession.sessionId,
                    slot = slot,
                    captcha = captcha,
                    centerId = currCenter.centerId
                )
                when(val scheduleAppointmentResp = scheduleAppointmentUseCase.execute(reqModel)) {
                    is ApiResult.Success -> {
                        _appointmentScheduleSuccess.postValue(resourceProvider
                            .getString(R.string.booking_success)
                            .format(scheduleAppointmentResp.value.appointmentConfirmationNo))
                        Timber.d("Success fully booked appointment: ${scheduleAppointmentResp.value.appointmentConfirmationNo}")
                    }
                    is ApiResult.NetworkError -> {
                        Timber.d("Check your network connection")
                        _appointmentScheduleFailed.postValue(scheduleAppointmentResp.error)
                    }
                    is ApiResult.GenericError -> {
                        Timber.d("Error happened: %d,%s, %s", scheduleAppointmentResp.code, scheduleAppointmentResp.error.error, scheduleAppointmentResp.error.errorCode)
                        if(scheduleAppointmentResp.code == 401) {
                            _finishBooking.postValue(true)
                        } else {
                            fetchCaptcha()
                            _appointmentScheduleFailed.postValue(scheduleAppointmentResp.error.error)
                        }
                    }
                }
            } else{
                Timber.d("Finishing booking at 134")
                _finishBooking.postValue(true)
            }
            _scheduleAppointment.postValue(false)
        }

    }


    private suspend fun getReCaptcha(): String? {
        var tryCount = 0
        while(tryCount < 3) {
            val captchaResp = getReCaptchaUseCase.execute(Unit)
            tryCount += 1
            when(captchaResp) {
                is ApiResult.Success -> {
                    Timber.d(captchaResp.value.captcha)
                    return captchaResp.value.captcha
                }
                is ApiResult.NetworkError -> {
                    Timber.d(captchaResp.error)
                    delay(2000)
                }
                is ApiResult.GenericError -> {
                    Timber.d(captchaResp.error.error)
                    if(captchaResp.code == 401) {
                        Timber.d("Finishing booking at 157")
                        _finishBooking.postValue(true)
                        break
                    } else {
                        delay(5000)
                    }
                }
            }
        }
        return null
    }

    private fun getHTMLBody(svgString: String) = "<html><body><img src=\"data:image/svg+xml;base64,$svgString\" /></body></html>"
}