package dev.romio.cowinvaccinebook.view.fragment

import android.content.Intent
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import dev.romio.cowinvaccinebook.R
import dev.romio.cowinvaccinebook.constant.AppConstant
import dev.romio.cowinvaccinebook.databinding.FragmentBeneficiaryDetailsBinding
import dev.romio.cowinvaccinebook.service.CowinBookingService
import dev.romio.cowinvaccinebook.usecase.model.*
import dev.romio.cowinvaccinebook.util.gone
import dev.romio.cowinvaccinebook.util.invisible
import dev.romio.cowinvaccinebook.util.visible
import dev.romio.cowinvaccinebook.view.adapter.BeneficiaryRvAdapter
import dev.romio.cowinvaccinebook.viewmodel.BeneficiaryDetailsViewModel
import dev.romio.cowinvaccinebook.viewmodel.MainActivityViewModel


@AndroidEntryPoint
class BeneficiaryDetailsFragment:
    BaseFragment<BeneficiaryDetailsViewModel, FragmentBeneficiaryDetailsBinding>() {

    private val beneficiariesAdapter by lazy {
        BeneficiaryRvAdapter()
    }

    override val viewModel: BeneficiaryDetailsViewModel by viewModels()
    private val activityViewModel: MainActivityViewModel by activityViewModels()

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBeneficiaryDetailsBinding = DataBindingUtil.inflate(inflater,
        R.layout.fragment_beneficiary_details, container, false)

    override fun setUpView() {
        super.setUpView()
        binding.rvBeneficiaries.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.rvBeneficiaries.adapter = beneficiariesAdapter
    }

    override fun initListeners() {
        super.initListeners()
        binding.btnRetry.setOnClickListener {
            viewModel.generateOtpIfRequired()
        }
        binding.acsStates.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                viewModel.fetchDistricts(p2)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                // Do Nothing
            }
        }
        binding.btnStartStopBooking.setOnClickListener {
            if(binding.btnStartStopBooking.text == getString(R.string.start_booking)) {
                getUserPrefAndValidate()
            } else {
                val intent = Intent(requireContext(), CowinBookingService::class.java)
                intent.action = AppConstant.STOP_FOREGROUND_ACTION
                requireActivity().startService(intent)
            }
        }

        binding.rgDistrictPinPref.setOnCheckedChangeListener { _, id ->
            when(id) {
                R.id.rb_district -> {
                    binding.llStatePin.visible()
                    binding.etPin.gone()
                }
                R.id.rb_pin -> {
                    binding.llStatePin.gone()
                    binding.etPin.visible()
                }
            }
        }
    }

    override fun observeViewModel() {
        super.observeViewModel()
        viewLifecycleOwner.apply {
            viewModel.onShowLoading.observe(this) {
                binding.pbLoading.visible()
                if(it) {
                    binding.nsvBeneficiaryDetails.invisible()
                    binding.clLoading.visible()
                } else {
                    binding.nsvBeneficiaryDetails.visible()
                    binding.clLoading.gone()
                }
            }

            viewModel.onShowRetry.observe(this) {
                if(it) {
                    binding.btnRetry.visible()
                    binding.pbLoading.invisible()
                } else {
                    binding.btnRetry.gone()
                }
            }

            viewModel.onShowLoadingStatus.observe(this) {
                binding.tvLoadingStatus.text = it
            }

            viewModel.onBeneficiariesFound.observe(this) {
                binding.tvNoBeneficiaries.gone()
                binding.rvBeneficiaries.visible()
                beneficiariesAdapter.setBeneficiaries(it)
            }

            viewModel.onShowDistrictsLoader.observe(this) {
                if(it) {
                    binding.pbDistricts.visible()
                    binding.acsDistricts.invisible()
                }
            }

            viewModel.onStates.observe(this) {
                val adapter = ArrayAdapter(requireContext(),
                    android.R.layout.simple_spinner_item, it.map { it.stateName ?: "" })
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.acsStates.adapter = adapter
                binding.pbStates.invisible()
                if(activityViewModel.serviceRunning.value != true) {
                    binding.acsStates.visible()
                }
            }

            viewModel.onDistricts.observe(this) {
                val adapter = ArrayAdapter(requireContext(),
                    android.R.layout.simple_spinner_item, it.map { it.districtName ?: "" })
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.acsDistricts.adapter = adapter
                binding.pbDistricts.invisible()
                if(activityViewModel.serviceRunning.value != true) {
                    binding.acsDistricts.visible()
                }
            }

            viewModel.validationStatus.observe(this) {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }

            viewModel.onStartService.observe(this) {
                val serviceIntent = Intent(requireContext(), CowinBookingService::class.java)
                requireActivity().startService(serviceIntent)
            }

            activityViewModel.serviceRunning.observe(this) {
                viewModel.fetchUserPreference(it)
                if(it) {
                    binding.btnStartStopBooking.text = getString(R.string.stop_booking)
                } else {
                    binding.btnStartStopBooking.text = getString(R.string.start_booking)
                }
            }

            viewModel.enableView.observe(this) {
                if(it.second != null) {
                    enableViews(it.first, it.second!!)
                }
            }
        }
    }

    private fun enableViews(enable: Boolean, userPreference: UserPreference) {
        when(userPreference.vaccineType) {
            VaccineType.NONE -> binding.rbNoVaccinePreference.isChecked = true
            VaccineType.COVISHIELD -> binding.rbCovishield.isChecked = true
            VaccineType.COVAXIN -> binding.rbCovaxin.isChecked = true
        }
        binding.rbNoVaccinePreference.isEnabled = enable
        binding.rbCovishield.isEnabled = enable
        binding.rbCovaxin.isEnabled = enable

        when(userPreference.districtOrPinPref) {
            PinOrDistrictPref.PIN -> binding.rbPin.isChecked = true
            PinOrDistrictPref.DISTRICT -> binding.rbDistrict.isChecked = true
        }
        binding.rbPin.isEnabled = enable
        binding.rbDistrict.isEnabled = enable
        binding.etPin.setText(userPreference.pin ?: "")
        binding.etPin.isEnabled = enable
        if(enable) {
            binding.acsStates.visible()
            binding.tvStateName.invisible()
            binding.acsDistricts.visible()
            binding.tvDistrictName.invisible()
        } else {
            binding.acsStates.invisible()
            binding.tvStateName.visible()
            binding.acsDistricts.invisible()
            binding.tvDistrictName.visible()
            binding.tvStateName.setText(userPreference.stateName)
            binding.tvDistrictName.setText(userPreference.districtName)
        }

        when(userPreference.feeTypePref) {
            FeeType.NONE -> binding.rbNoFreePref.isChecked = true
            FeeType.FREE -> binding.rbFreeYes.isChecked = true
            FeeType.PAID -> binding.rbFreeNo.isChecked = true
        }
        binding.rbNoFreePref.isEnabled = enable
        binding.rbFreeYes.isEnabled = enable
        binding.rbFreeNo.isEnabled = enable

        binding.etPollingFrequency.setText(userPreference.refreshInterval.toString())
        binding.etPollingFrequency.isEnabled = enable

        when(userPreference.ageGroup) {
            AgeGroup.EIGHTEEN_PLUS -> binding.rbEighteenPlus.isChecked = true
            AgeGroup.FORTY_FIVE_PLUS -> binding.rbFourtyFivePlus.isChecked = true
        }
        binding.rbEighteenPlus.isEnabled = enable
        binding.rbFourtyFivePlus.isEnabled = enable

        beneficiariesAdapter.enableChecking(enable)
        viewModel.onStates.value?.indexOfFirst { it.stateId == userPreference.stateId }?.let {
            binding.acsStates.setSelection(it)
        }
        viewModel.onDistricts.value?.indexOfFirst { it.districtId == userPreference.districtId }?.let {
            binding.acsDistricts.setSelection(it)
        }

    }


    private fun getUserPrefAndValidate() {
        val vaccineType = when(binding.rgVaccinePreference.checkedRadioButtonId) {
            R.id.rb_no_vaccine_preference -> VaccineType.NONE
            R.id.rb_covaxin -> VaccineType.COVAXIN
            R.id.rb_covishield -> VaccineType.COVISHIELD
            else ->  VaccineType.NONE
        }

        val freeVaccinePref = when(binding.rgFreeVaccine.checkedRadioButtonId) {
            R.id.rb_no_free_pref -> FeeType.NONE
            R.id.rb_free_yes -> FeeType.FREE
            R.id.rb_free_no -> FeeType.PAID
            else -> FeeType.NONE
        }

        val ageGroup = when(binding.rgLookingFor.checkedRadioButtonId) {
            R.id.rb_fourty_five_plus -> AgeGroup.FORTY_FIVE_PLUS
            R.id.rb_eighteen_plus -> AgeGroup.EIGHTEEN_PLUS
            else -> AgeGroup.EIGHTEEN_PLUS
        }

        val districtOrPinPref = when(binding.rgDistrictPinPref.checkedRadioButtonId) {
            R.id.rb_pin -> PinOrDistrictPref.PIN
            R.id.rb_district -> PinOrDistrictPref.DISTRICT
            else -> PinOrDistrictPref.PIN
        }

        val selectedStatePosition = binding.acsStates.selectedItemPosition
        val selectedDistrictPosition = binding.acsDistricts.selectedItemPosition
        val refreshInterval = binding.etPollingFrequency.text?.toString()
        val pin = binding.etPin.text?.toString()

        viewModel.validateUserPref(vaccineType, freeVaccinePref, ageGroup,
            selectedStatePosition, selectedDistrictPosition, refreshInterval, districtOrPinPref, pin)
    }
}


