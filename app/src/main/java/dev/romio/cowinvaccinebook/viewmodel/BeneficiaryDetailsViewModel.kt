package dev.romio.cowinvaccinebook.viewmodel

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.romio.cowinvaccinebook.R
import dev.romio.cowinvaccinebook.data.model.ApiResult
import dev.romio.cowinvaccinebook.data.model.BeneficiarySummary
import dev.romio.cowinvaccinebook.data.model.District
import dev.romio.cowinvaccinebook.data.model.State
import dev.romio.cowinvaccinebook.repository.CowinAppRepository
import dev.romio.cowinvaccinebook.usecase.*
import dev.romio.cowinvaccinebook.usecase.model.*
import dev.romio.cowinvaccinebook.util.ResourceProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BeneficiaryDetailsViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val shouldGenerateBearerTokenUseCase: ShouldGenerateBearerTokenUseCase,
    private val generateOTPUseCase: GenerateOTPUseCase,
    private val observeOTPReceiveUseCase: ObserveOTPReceiveUseCase,
    private val generateBearerTokenUseCase: GenerateBearerTokenUseCase,
    private val fetchBeneficiaryDetailsUseCase: FetchBeneficiaryDetailsUseCase,
    private val observeBeneficiarySummaryUseCase: ObserveBeneficiarySummaryUseCase,
    private val getStatesUseCase: GetStatesUseCase,
    private val getDistrictsForStateUseCase: GetDistrictsForStateUseCase,
    private val saveUserPreferenceUseCase: SaveUserPreferenceUseCase,
    private val cowinAppRepository: CowinAppRepository
): BaseViewModel() {

    private val _showLoading = MutableLiveData<Boolean>()
    private val _showLoadingStatus = MutableLiveData<String>()
    private val _showRetry = MutableLiveData<Boolean>()
    private val _beneficiaries = MutableLiveData<List<BeneficiarySummary>>()
    private val _states = MutableLiveData<List<State>>()
    private val _districts = MutableLiveData<List<District>>()
    private val _showDistrictsLoader = MutableLiveData<Boolean>()
    private val _validationStatus = MutableLiveData<String>()
    private val _startService = MutableLiveData<Boolean>()
    private val _enableView = MutableLiveData<Pair<Boolean, UserPreference?>>()

    private var timer: CountDownTimer? = null

    val onShowLoading: LiveData<Boolean> = _showLoading
    val onShowLoadingStatus: LiveData<String> = _showLoadingStatus
    val onShowRetry: LiveData<Boolean> = _showRetry
    val onBeneficiariesFound: LiveData<List<BeneficiarySummary>> = _beneficiaries
    val onStates: LiveData<List<State>> = _states
    val onDistricts: LiveData<List<District>> = _districts
    val onShowDistrictsLoader: LiveData<Boolean> = _showDistrictsLoader
    val validationStatus: LiveData<String> = _validationStatus
    val onStartService: LiveData<Boolean> = _startService
    val enableView: LiveData<Pair<Boolean, UserPreference?>> = _enableView

    override fun onViewCreate() {
        super.onViewCreate()
        observeOnOtpReceived()
        generateOtpIfRequired()
        fetchStates()
    }

    private fun fetchStates() {
        // TODO: Add Retry for fetching States
        viewModelScope.launch {
            val statesApiResp = getStatesUseCase.execute(Unit)
            if(statesApiResp is ApiResult.Success) {
                val states = statesApiResp.value.states ?: arrayListOf()
                _states.postValue(states)
            }
        }
    }

    fun fetchDistricts(position: Int) {
        val stateId = _states.value?.get(position)?.stateId ?: return
        viewModelScope.launch {
            _showDistrictsLoader.postValue(true)
            val districtApiResp = getDistrictsForStateUseCase.execute(stateId)
            if(districtApiResp is ApiResult.Success) {
                val districts = districtApiResp.value.districts ?: arrayListOf()
                _districts.postValue(districts)
                _showDistrictsLoader.postValue(false)
            }
        }
    }

    fun fetchUserPreference(isServiceRunning: Boolean) {
        viewModelScope.launch {
            val preference = cowinAppRepository.getUserPreference()
            if(preference?.stateId != null) {
                val districtApiResp = getDistrictsForStateUseCase.execute(preference.stateId)
                if (districtApiResp is ApiResult.Success) {
                    val districts = districtApiResp.value.districts ?: arrayListOf()
                    _districts.postValue(districts)
                    _showDistrictsLoader.postValue(false)
                }
            }
            _enableView.postValue(Pair(!isServiceRunning, preference))
        }
    }

    fun generateOtpIfRequired() {
        // TODO : Handle case when otp already exists
        viewModelScope.launch {
            _showRetry.postValue(false)
            _showLoading.postValue(true)
            if(shouldGenerateBearerTokenUseCase.execute(Unit)) {
                _showLoadingStatus.postValue(resourceProvider.getString(R.string.generating_otp))
                when(val apiResp = generateOTPUseCase.execute(Unit)) {
                    is ApiResult.Success -> {
                        _showLoadingStatus.postValue(resourceProvider.getString(R.string.otp_generated))
                        createAndStartTimer()
                    }
                    is ApiResult.NetworkError -> {
                        Timber.d(apiResp.error)
                        _showLoadingStatus.postValue(apiResp.error)
                        _showRetry.postValue(true)
                    }
                    is ApiResult.GenericError -> {
                        Timber.d(apiResp.toString())
                        _showLoadingStatus.postValue(apiResp.toString())
                        _showRetry.postValue(true)
                    }
                }
            } else {
                fetchBeneficiaryDetails()
            }
        }
    }

    private fun observeOnOtpReceived() {
        viewModelScope.launch {
            observeOTPReceiveUseCase.execute(Unit).collect {
                Timber.d("OTP Received: $it")
                _showLoadingStatus.postValue(resourceProvider.getString(R.string.otp_received).format(it))
                timer?.cancel()
                delay(500)
                _showLoadingStatus.postValue(resourceProvider.getString(R.string.validating_otp))
                Timber.d("Generating Bearer Token/ Validating OTP")
                when(val apiResp = generateBearerTokenUseCase.execute(Unit)) {
                    is ApiResult.Success -> {
                        Timber.d("OTO Validated")
                        _showLoadingStatus.postValue(resourceProvider.getString(R.string.otp_validated))
                        fetchBeneficiaryDetails()
                    }
                    is ApiResult.NetworkError -> {
                        Timber.d(apiResp.error)
                        _showLoadingStatus.postValue(apiResp.error)
                        _showRetry.postValue(true)
                    }
                    is ApiResult.GenericError -> {
                        Timber.d(apiResp.toString())
                        _showLoadingStatus.postValue(apiResp.toString())
                        _showRetry.postValue(true)
                    }
                }
            }
        }
    }
    private fun observeBeneficiarySummary() {
        viewModelScope.launch {
            observeBeneficiarySummaryUseCase.execute(Unit).collect {
                _showLoading.postValue(false)
                _beneficiaries.postValue(it)
            }
        }
    }

    private suspend fun fetchBeneficiaryDetails() {
        observeBeneficiarySummary()
        viewModelScope.launch {
            _showLoadingStatus.postValue(resourceProvider.getString(R.string.fetching_beneficiary_details))
            when(val apiResp = fetchBeneficiaryDetailsUseCase.execute(Unit)) {
                is ApiResult.GenericError -> {
                    Timber.d(apiResp.toString())
                    _showLoading.postValue(true)
                    _showLoadingStatus.postValue(apiResp.toString())
                    _showRetry.postValue(true)
                }
                is ApiResult.NetworkError -> {
                    Timber.d(apiResp.error)
                    _showLoading.postValue(true)
                    _showLoadingStatus.postValue(apiResp.error)
                    _showRetry.postValue(true)
                }
                else -> {
                    // Do Nothing
                }
            }
        }
    }

    private fun createAndStartTimer() {
        timer = object : CountDownTimer(180000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _showLoadingStatus.postValue(resourceProvider.getString(R.string.otp_timer).format(millisUntilFinished/1000))
            }

            override fun onFinish() {
                Timber.d("Timer Finished")
                generateOtpIfRequired()
            }
        }
        timer?.start()
    }

    fun validateUserPref(
        vaccineType: VaccineType,
        feeTypePref: FeeType,
        ageGroup: AgeGroup,
        selectedStatePosition: Int,
        selectedDistrictPosition: Int,
        refreshInterval: String?,
        districtOrPinPref: PinOrDistrictPref,
        pin: String?
    ) {
        val checkedBeneficiaries = _beneficiaries.value?.filter { it.isChecked == true } ?: arrayListOf()

        if(checkedBeneficiaries.isEmpty()) {
            _validationStatus.postValue(resourceProvider.getString(R.string.select_at_least_one))
            return
        }
        if(checkedBeneficiaries.any { it.age < ageGroup.age }) {
            _validationStatus.postValue(resourceProvider.getString(R.string.age_issue))
            return
        }
        if(districtOrPinPref == PinOrDistrictPref.DISTRICT && (selectedDistrictPosition < 0 || selectedStatePosition < 0)) {
            _validationStatus.postValue(resourceProvider.getString(R.string.inavalid_state_or_district))
            return
        }
        if(refreshInterval.isNullOrEmpty()) {
            _validationStatus.postValue(resourceProvider.getString(R.string.invalid_refresh_interval))
            return
        }
        if(districtOrPinPref == PinOrDistrictPref.PIN && pin.isNullOrEmpty()) {
            _validationStatus.postValue(resourceProvider.getString(R.string.enter_pin))
            return
        }
        val selectedState = _states.value?.get(selectedStatePosition)
        val selectedDistrict = _districts.value?.get(selectedDistrictPosition)
        var refreshIntervalFinal = refreshInterval.toInt()
        if(refreshIntervalFinal < 3) {
            refreshIntervalFinal = 3
        } else if(refreshIntervalFinal > 15) {
            refreshIntervalFinal = 15
        }
        if(districtOrPinPref == PinOrDistrictPref.DISTRICT && (selectedState == null || selectedDistrict == null)) {
            _validationStatus.postValue(resourceProvider.getString(R.string.inavalid_state_or_district))
            return
        }
        if(_beneficiaries.value != null) {
            viewModelScope.launch {
                saveUserPreferenceUseCase.execute(UserPreferenceAndBeneficiaries(
                    userPreference = UserPreference(
                        vaccineType = vaccineType,
                        feeTypePref = feeTypePref,
                        ageGroup = ageGroup,
                        stateId = if(districtOrPinPref == PinOrDistrictPref.DISTRICT) selectedState?.stateId else null,
                        stateName = if(districtOrPinPref == PinOrDistrictPref.DISTRICT) selectedState?.stateName else null,
                        districtId = if(districtOrPinPref == PinOrDistrictPref.DISTRICT) selectedDistrict?.districtId else null,
                        districtName = if(districtOrPinPref == PinOrDistrictPref.DISTRICT) selectedDistrict?.districtName else null,
                        refreshInterval = refreshIntervalFinal,
                        districtOrPinPref = districtOrPinPref,
                        pin = if(districtOrPinPref == PinOrDistrictPref.PIN) pin else null
                    ),
                    beneficiaries = _beneficiaries.value!!
                ))
                _startService.postValue(true)
            }
        }


    }
}