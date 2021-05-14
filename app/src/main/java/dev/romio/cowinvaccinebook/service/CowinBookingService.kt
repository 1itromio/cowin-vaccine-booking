package dev.romio.cowinvaccinebook.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import dev.romio.cowinvaccinebook.R
import dev.romio.cowinvaccinebook.constant.AppConstant
import dev.romio.cowinvaccinebook.data.model.ApiResult
import dev.romio.cowinvaccinebook.data.model.BeneficiarySummary
import dev.romio.cowinvaccinebook.data.model.Center
import dev.romio.cowinvaccinebook.data.model.Session
import dev.romio.cowinvaccinebook.repository.CowinAppRepository
import dev.romio.cowinvaccinebook.usecase.*
import dev.romio.cowinvaccinebook.usecase.model.*
import dev.romio.cowinvaccinebook.view.activity.CowinBookingActivity
import dev.romio.cowinvaccinebook.view.activity.MainActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@AndroidEntryPoint
class CowinBookingService: Service() {

    companion object {
        const val CHANNEL_ID = "dev.romio.cowinvaccinebook.CowinBookingChannel"
        private const val NOTIFICATION_ID = 3455
    }

    @Inject
    lateinit var cowinAppRepository: CowinAppRepository

    @Inject
    lateinit var observeOTPReceiveUseCase: ObserveOTPReceiveUseCase

    @Inject
    lateinit var shouldGenerateBearerTokenUseCase: ShouldGenerateBearerTokenUseCase

    @Inject
    lateinit var shouldRequestOTPUseCase: ShouldRequestOTPUseCase

    @Inject
    lateinit var fetchVaccinationCentersUseCase: FetchVaccinationCentersUseCase

    @Inject
    lateinit var generateBearerTokenUseCase: GenerateBearerTokenUseCase

    @Inject
    lateinit var generateOTPUseCase: GenerateOTPUseCase

    private var userPreference: UserPreference? = null
    private var otpTimer: CountDownTimer? = null

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private val publishableMessages = LinkedList<String>()

    private val lookingForVaccine = AtomicBoolean(false)

    //private var tryCount = 0

    private val notificationBuilder by lazy {
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Cowin Vaccine appointment")
            .setAutoCancel(false)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOnlyAlertOnce(true)
            .setSmallIcon(android.R.drawable.ic_menu_help)
            .addAction(android.R.drawable.ic_media_pause,"STOP",
                PendingIntent.getService(applicationContext, 11,
                    Intent(applicationContext, CowinBookingService::class.java)
                        .setAction(AppConstant.STOP_FOREGROUND_ACTION), 0))
            .setContentIntent(
                PendingIntent.getActivity(applicationContext, 10,
                    Intent(applicationContext, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
                    0)
            )
    }

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    var foundBookingCenters: List<Center>? = null
    var foundBookingSesionMap: Map<Int, List<Session>>? = null

    private val binder by lazy {
        ServiceBinder()
    }

    override fun onBind(p0: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        Timber.d("Started Foreground Service")
        startNotification()
        observeOTP()
        start()
        cowinAppRepository.saveServiceRunning(true)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            AppConstant.STOP_FOREGROUND_ACTION -> stopServiceGraceFully()
        }
        return START_STICKY
    }


    inner class ServiceBinder: Binder() {
        val service: CowinBookingService
            get() = this@CowinBookingService
    }

    private fun observeOTP() {
        serviceScope.launch {
            publishStatus("Started Observing OTP")
            observeOTPReceiveUseCase.execute(Unit).collect {
                otpTimer?.cancel()
                publishStatus("Received OTP: $it", true)
                generateBearerToken()
            }
        }
    }

    private fun start() {
        serviceScope.launch {
            lookingForVaccine.compareAndSet(false, true)
            publishStatus("Starting Looking for vaccines")
            userPreference = cowinAppRepository.getUserPreference()
            if(userPreference == null) {
                publishStatus("User Preference Not Found. Stopping Service Gracefully")
                withContext(Dispatchers.Main) {
                    stopServiceGraceFully()
                }
            } else {
                checkVaccineAvailability()
            }
        }
    }

    private suspend fun checkVaccineAvailability() {
        publishStatus("Starting to check vaccine availability", true)
        var isVaccineAvailable = false
        var shouldGenerateBearerToken = false
        val cowinCalendarRequest = if(userPreference?.districtOrPinPref == PinOrDistrictPref.PIN) {
                CowinCalendarRequest.PinCowinCalendarRequest(userPreference?.pin!!)
            } else {
                CowinCalendarRequest.DistrictCowinCalendarRequest(userPreference?.districtId!!)
            }
        val beneficiaries = cowinAppRepository.getSavedBeneficiaryDetails().filter { it.isChecked == true }
        val refreshInterval = (userPreference?.refreshInterval ?: 6) * 1000L
        while(!isVaccineAvailable && !shouldGenerateBearerToken) {
            shouldGenerateBearerToken = shouldGenerateBearerTokenUseCase.execute(Unit)
            publishStatus("Checking for vaccines", true)
            val covidCentersResp = fetchVaccinationCentersUseCase.execute(cowinCalendarRequest)
            if(covidCentersResp is ApiResult.Success) {
                val filteredCentersPair = filterAvailableCenters(covidCentersResp.value.centers, beneficiaries)
                if(filteredCentersPair.first.isNotEmpty()) {
                    isVaccineAvailable = true
                    publishStatus("Vaccines Available, Initiating Schedule Appointment", true)
                    lookingForVaccine.compareAndSet(true, false)
                    initiateBooking(filteredCentersPair.first, filteredCentersPair.second)
                } else {
                    publishStatus("No vaccines available. Waiting for ${refreshInterval/1000} secs", true)
                    delay(refreshInterval)
                }
            } else {
                publishStatus("Got Error while trying to get available centers. Waiting for ${refreshInterval/1000} secs", true)
                delay(refreshInterval)
            }
        }
        if(shouldGenerateBearerToken) {
            generateBearerToken()
        }
    }

    private suspend fun generateBearerToken() {
        if(shouldRequestOTPUseCase.execute(Unit)) {
            publishStatus("Need to Request OTP. Requesting OTP", true)
            when(generateOTPUseCase.execute(Unit)) {
                is ApiResult.Success -> {
                    publishStatus("OTP Requested Successfully", true)
                    startOTPTimer()
                }
                else -> {
                    publishStatus("Failed to request OTP, Retrying after 5Sec", true)
                    delay(5000)
                    generateBearerToken()
                }
            }
        } else {
            publishStatus("Need to Generate Bearer Token. Generating Bearer Token", true)
            when(generateBearerTokenUseCase.execute(Unit)) {
                is ApiResult.Success -> {
                    delay(100)
                    publishStatus("Successfully Generated bearer token", true)
                    if(lookingForVaccine.get()) {
                        checkVaccineAvailability()
                    }
                }
                is ApiResult.NetworkError -> {
                    publishStatus("Failed to generate bearer token, Retrying after 3Sec", true)
                    delay(3000)
                    generateBearerToken()
                }
                is ApiResult.GenericError -> {
                    publishStatus("Failed to generate bearer token, Retrying after 6Sec", true)
                    delay(6000)
                    generateBearerToken()
                }
            }
        }
    }

    private suspend fun initiateBooking(
        centers: List<Center>,
        centerToSessionsMap: Map<Int, List<Session>>
    ) = withContext(Dispatchers.Main) {
        foundBookingCenters = centers
        foundBookingSesionMap = centerToSessionsMap
        val intent = Intent(applicationContext, CowinBookingActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    fun restartLooking() {
        Timber.d("Restart booking called")
        serviceScope.launch {
            lookingForVaccine.compareAndSet(false, true)
            checkVaccineAvailability()
        }
    }

    private fun filterAvailableCenters(
        centers: List<Center>?,
        beneficiaries: List<BeneficiarySummary>
    ): Pair<List<Center>, Map<Int, List<Session>>> {
        //tryCount++
        val centerIdToAvailableSessionMap = hashMapOf<Int, List<Session>>()
        val filteredCenters = centers?.filter { center ->
            // Uncomment for Testing
            /*if(tryCount > 5) {
                centerIdToAvailableSessionMap[center.centerId] = center.sessions ?: arrayListOf()
                return@filter true
            }*/
            val filteredSessions = center.sessions?.filter { session ->
                (session.availableCapacity ?: 0) >= beneficiaries.size &&
                        session.minAgeLimit == userPreference?.ageGroup?.age &&
                        (userPreference?.vaccineType == VaccineType.NONE ||
                                session.vaccine == userPreference?.vaccineType?.vaccine)
            } ?: arrayListOf()
            if(filteredSessions.isNotEmpty()) {
                centerIdToAvailableSessionMap[center.centerId] = filteredSessions
            }
            filteredSessions.isNotEmpty() && (userPreference?.feeTypePref == FeeType.NONE ||
                    userPreference?.feeTypePref?.type == center.feeType)
        } ?: arrayListOf()
        return Pair(filteredCenters, centerIdToAvailableSessionMap)
    }

    private suspend fun publishStatus(message: String,
                                      publishToNotification: Boolean = false) = withContext(Dispatchers.Main) {
        Timber.d(message)
        if(publishToNotification) {
            if(publishableMessages.size >= 7) {
                publishableMessages.poll()
            }
            publishableMessages.add("${System.currentTimeMillis()/1000}: $message")
            notificationBuilder.setContentText(message)
            notificationBuilder.setStyle(NotificationCompat.InboxStyle().let { style ->
                publishableMessages.reversed().forEach {
                    style.addLine(it)
                }
                style
            })
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        }
    }

    private fun startNotification() {
        createNotificationChannel()
        notificationBuilder.setContentText("Cowin Vaccine Booking Started")
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Cowin Booking Notification"
            val descriptionText = "Cowin Booking Notification"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startOTPTimer() {
        otpTimer = object: CountDownTimer(180000, 2000) {
            override fun onTick(millisUntilFinished: Long) {
                serviceScope.launch {
                    publishStatus("Waiting to Receive OTP in ${millisUntilFinished/1000} Secs", true)
                }
            }

            override fun onFinish() {
                serviceScope.launch {
                    publishStatus("Finishing OTP Timer")
                    generateBearerToken()
                }
            }
        }
    }

    private fun stopServiceGraceFully() {
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        cowinAppRepository.saveServiceRunning(false)
        serviceJob.cancel()
        super.onDestroy()
    }
}