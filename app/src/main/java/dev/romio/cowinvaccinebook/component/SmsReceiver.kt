package dev.romio.cowinvaccinebook.component

import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import dagger.hilt.android.AndroidEntryPoint
import dev.romio.cowinvaccinebook.data.preference.AppPreference
import dev.romio.cowinvaccinebook.repository.CowinAppRepository
import javax.inject.Inject


@AndroidEntryPoint
class SmsReceiver: HiltBroadcastReceiver() {

    companion object {
        private const val ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED"
        private const val SMS_CONTENT_TEXT = "Your OTP to register/access CoWIN is"
        private const val OTP_REGEX_STR = "\\d{6}"
    }

    @Inject
    lateinit var repo: CowinAppRepository
    private val otpRegex by lazy {
        OTP_REGEX_STR.toRegex()
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if(context == null || intent == null || intent.action == null) {
            return
        }
        if (intent.action != (Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            return
        }
        val contentResolver = context.contentResolver
        val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val otp = smsMessages.firstOrNull { message ->
            val text = message.messageBody
            text.contains(SMS_CONTENT_TEXT)
        }?.let { message ->
            otpRegex.find(message.messageBody)?.value
        }
        if(otp != null) {
            repo.saveOtp(otp.trim())
        }
    }
}