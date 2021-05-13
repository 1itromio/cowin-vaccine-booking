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
import org.apache.commons.text.StringEscapeUtils
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import javax.inject.Inject

@HiltViewModel
class CowinBookingActivityViewModel @Inject constructor(
    private val getReCaptchaUseCase: GetReCaptchaUseCase,
    private val scheduleAppointmentUseCase: ScheduleAppointmentUseCase,
    private val cowinAppRepository: CowinAppRepository,
    private val resourceProvider: ResourceProvider,
    private val application: Application
): BaseViewModel() {

    // TODO(Make Alert Sounds)
    //TODO(TAKE PERSMISSION FOR SYSTEM ALERT)
    // TODO Retry captchas

    private var centers: List<Center>? = null
    private var centerIdToSessionsMap: Map<Int, List<Session>>? = null
    private var currCenterCount = 0

    private val _currCenter = MutableLiveData<Center>()
    private val _currSessions = MutableLiveData<List<Session>>()
    private val _currCaptcha = MutableLiveData<String>()
    private val _appointmentScheduleSuccess = MutableLiveData<String>()
    private val _appointmentScheduleFailed = MutableLiveData<String>()
    private val _finishBooking = MutableLiveData<Boolean>()
    private val _fetchingCaptcha = MutableLiveData<Boolean>()
    private val _scheduleAppointment = MutableLiveData<Boolean>()

    val currCenter: LiveData<Center> = _currCenter
    val currSessions: LiveData<List<Session>> = _currSessions
    val currCaptcha: LiveData<String> = _currCaptcha
    val onAppointmentScheduleSuccess: LiveData<String> = _appointmentScheduleSuccess
    val onAppointmentScheduleFailed: LiveData<String> = _appointmentScheduleFailed
    val finishBooking: LiveData<Boolean> = _finishBooking
    val onFetchingCaptcha: LiveData<Boolean> = _fetchingCaptcha
    val onScheduleAppointment: LiveData<Boolean> = _scheduleAppointment

    var beneficiaries: List<BeneficiarySummary>? = null

    fun initiateBookingProcedure(
        centers: List<Center>?,
        centerIdToSessionsMap: MutableMap<Int, List<Session>>?
    ) {
        this.centers = centers
        this.centerIdToSessionsMap = centerIdToSessionsMap
        book()
    }

    fun book() {
        if(centers?.size?: 0 > currCenterCount) {
            val currCenter = centers?.get(currCenterCount)
            val currSessions = centerIdToSessionsMap?.get(currCenter?.centerId)
            viewModelScope.launch {
                if(currCenter != null && currSessions.isNullOrEmpty().not()) {
                    book(currCenter, currSessions!!)
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

    private suspend fun book(currCenter: Center, currSessions: List<Session>) {
        _currCenter.postValue(currCenter)
        _currSessions.postValue(currSessions)
        _fetchingCaptcha.postValue(true)
        val captcha = getReCaptcha()
        _fetchingCaptcha.postValue(false)
        if(captcha != null) {
            val cleanCaptcha = captcha.replace("(<path d=)(.*?)(fill=\"none\"/>)".toRegex(), "")
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

    fun scheduleAppointment(captcha: String) {
        viewModelScope.launch {
            _scheduleAppointment.postValue(true)
            if(beneficiaries == null) {
                beneficiaries = cowinAppRepository.getSavedBeneficiaryDetails()
            }
            val session = if(_currSessions.value?.size ?: 0 > 0) _currSessions.value?.get(0) else null
            val slot = if(session?.slots?.size ?: 0 > 0) session?.slots?.get(0) else null
            val center = _currCenter.value
            if(session != null && slot != null && center != null) {
                val reqModel = ScheduleAppointmentRequest(
                    beneficiaries = beneficiaries!!.filter { it.isChecked == true }.map { it.brId },
                    dose = 1,
                    sessionId = session.sessionId,
                    slot = slot,
                    captcha = captcha,
                    centerId = center.centerId
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
                        _appointmentScheduleFailed.postValue(scheduleAppointmentResp.error.error)
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

                    //val captchaString = StringEscapeUtils.unescapeXml(captchaResp.value.captcha.replace("\\", ""))
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

    /*private fun writeToFile(data: String): String? {
        val folder = File(application.externalCacheDir, "/captchas/")
        if (!folder.exists()) {
            if (!folder.mkdir()) {
                Timber.d("Cannot create a directory!")
            } else {
                folder.mkdirs();
            }
        }
        val file = File(folder, "${System.currentTimeMillis()}.svg")
        return try {
            val outputStream = FileOutputStream(java.lang.String.valueOf(file))
            val outputStreamWriter = OutputStreamWriter(outputStream)
            outputStreamWriter.write(data)
            outputStreamWriter.close()
            file.path
        } catch (e: IOException) {
            Timber.e(e)
            null
        }
    }*/
}