package dev.romio.cowinvaccinebook.view.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.*
import android.view.inputmethod.EditorInfo
import android.widget.RadioButton
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.children
import androidx.core.view.get
import androidx.core.widget.doOnTextChanged
import dagger.hilt.android.AndroidEntryPoint
import dev.romio.cowinvaccinebook.R
import dev.romio.cowinvaccinebook.data.model.Session
import dev.romio.cowinvaccinebook.databinding.ActivityCowinBookingBinding
import dev.romio.cowinvaccinebook.service.CowinBookingService
import dev.romio.cowinvaccinebook.util.gone
import dev.romio.cowinvaccinebook.util.invisible
import dev.romio.cowinvaccinebook.util.visible
import dev.romio.cowinvaccinebook.viewmodel.CowinBookingActivityViewModel
import timber.log.Timber


@AndroidEntryPoint
class CowinBookingActivity: BaseActivity<CowinBookingActivityViewModel>() {
    override val viewModel: CowinBookingActivityViewModel by viewModels()

    private lateinit var bookingService: CowinBookingService
    private var isBound: Boolean = false
    private lateinit var binding: ActivityCowinBookingBinding
    private var ringtone: Ringtone? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as CowinBookingService.ServiceBinder
            bookingService = binder.service
            isBound = true
            viewModel.initiateBookingProcedure(bookingService.foundBookingCenters?.toMutableList(),
                bookingService.foundBookingSesionMap?.toMutableMap())
        }
        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCowinBookingBinding.inflate(layoutInflater)
        Timber.d("Setting content view")
        setContentView(binding.root)
        initListeners()
        observeViewModel()
        playVaccineFoundAlert()
    }

    override fun onStart() {
        super.onStart()
        // Bind to LocalService
        Timber.d("OnStart")
        Intent(this, CowinBookingService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun initListeners() {
        binding.btnSchedule.setOnClickListener {
            trySchedulingAppointment()
        }

        binding.etCaptcha.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                trySchedulingAppointment()
            }
            false
        })

        binding.etCaptcha.doOnTextChanged { _, _, _, _ ->
            if(ringtone?.isPlaying == true) {
                ringtone?.stop()
            }
        }

        binding.btnCancel.setOnClickListener {
            bookingService.restartLooking()
            finish()
        }

        binding.btnNextCenter.setOnClickListener {
            viewModel.bookNextCenter()
        }
        binding.btnNextSession.setOnClickListener {
            viewModel.bookNextSession()
        }
    }

    private fun observeViewModel() {
        binding.wvCaptcha.settings.allowFileAccess = true
        viewModel.currCenter.observe(this) {
            //binding.etCaptcha.setText("")
            binding.center = it
            binding.tvTotalCenters.text =
                getString(R.string.total_available_centers)
                    .format("${viewModel.currCenterCount + 1}/${(viewModel.centers?.size ?: 0)}")
            binding.executePendingBindings()
        }
        viewModel.currSession.observe(this) {
            setCurrSessionData(it)
        }
        viewModel.currCaptcha.observe(this) {
            binding.etCaptcha.setText("")
            binding.wvCaptcha.visible()
            binding.pbCaptchaLoading.invisible()
            Timber.d("Setting Captcha: $it")
            binding.wvCaptcha.loadData(it, "text/html", "UTF-8")
        }
        viewModel.onAppointmentScheduleFailed.observe(this) {
            vibrate(500)
            binding.tvScheduleStatus.text = it
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            viewModel.bookNextSession()
        }
        viewModel.onAppointmentScheduleSuccess.observe(this) {
            vibrate(2000)
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            binding.btnSchedule.gone()
            binding.btnNextCenter.gone()
            binding.btnNextSession.gone()
            binding.etCaptcha.gone()
            binding.wvCaptcha.gone()
            binding.pbCaptchaLoading.gone()
            binding.rgSlots.gone()
            binding.tvScheduleStatus.text = it
        }
        viewModel.finishBooking.observe(this) {
            Timber.d("Finish booking called")
            bookingService.restartLooking()
            finish()
        }
        viewModel.onFetchingCaptcha.observe(this) {
            if(it) {
                binding.etCaptcha.setText("")
                binding.tvScheduleStatus.text = getString(R.string.fetching_captcha)
                //binding.ivCaptcha.invisible()
                binding.pbCaptchaLoading.visible()
            } else {
                binding.pbCaptchaLoading.invisible()
            }
        }
        viewModel.onScheduleAppointment.observe(this) {
            if(it) {
                binding.etCaptcha.isEnabled = false
                binding.btnSchedule.isEnabled = false
                binding.btnNextSession.isEnabled = false
                binding.btnNextCenter.isEnabled = false
                binding.tvScheduleStatus.text = getString(R.string.trying_to_schedule)
            } else {
                binding.etCaptcha.isEnabled = true
                binding.btnSchedule.isEnabled = true
                binding.btnNextSession.isEnabled = true
                binding.btnNextCenter.isEnabled = true
            }
        }
    }

    private fun trySchedulingAppointment() {
        if(ringtone?.isPlaying == true) {
            ringtone?.stop()
        }
        val captcha = binding.etCaptcha.text?.toString()
        if(captcha.isNullOrEmpty().not()) {
            val checkedRb = binding.rgSlots.children.find { view ->
                (view as RadioButton).isChecked
            } as RadioButton
            viewModel.scheduleAppointment(captcha!!, checkedRb.text.toString())
        } else {
            vibrate(500)
            Toast.makeText(this, getString(R.string.invalid_captcha), Toast.LENGTH_SHORT).show()
        }
    }

    private fun setCurrSessionData(session: Session) {
        binding.session = session
        binding.tvCurrSession.text = "${viewModel.currSessionCountForCenter + 1}/${viewModel.currCenter.value?.sessions?.size}"
        binding.executePendingBindings()
        binding.rgSlots.removeAllViews()
        session.slots?.forEach {
            val radioButton = RadioButton(this)
            radioButton.text = it
            binding.rgSlots.addView(radioButton)
        }
        (binding.rgSlots[0] as RadioButton).isChecked = true
    }

    override fun onStop() {
        unbindService(connection)
        isBound = false
        super.onStop()
    }

    private fun playVaccineFoundAlert() {
        Timber.d("Playing vaccine alert")
        val alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        if(alert != null) {
            ringtone = RingtoneManager.getRingtone(applicationContext, alert)
            ringtone?.play()
        }
    }

    private fun vibrate(timeInMillis: Long){
        val v = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(timeInMillis, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            v.vibrate(timeInMillis)
        }
    }
}