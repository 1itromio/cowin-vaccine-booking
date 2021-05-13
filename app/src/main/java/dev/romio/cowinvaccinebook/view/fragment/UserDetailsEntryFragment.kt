package dev.romio.cowinvaccinebook.view.fragment

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fondesa.kpermissions.allGranted
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.fondesa.kpermissions.extension.send
import dagger.hilt.android.AndroidEntryPoint
import dev.romio.cowinvaccinebook.R
import dev.romio.cowinvaccinebook.databinding.FragmentUserDetailsEntryBinding
import dev.romio.cowinvaccinebook.viewmodel.MainActivityViewModel
import dev.romio.cowinvaccinebook.viewmodel.UserDetailsEntryViewModel


@AndroidEntryPoint
class UserDetailsEntryFragment : BaseFragment<UserDetailsEntryViewModel, FragmentUserDetailsEntryBinding>() {


    override val viewModel: UserDetailsEntryViewModel by viewModels()

    private val activityViewModel: MainActivityViewModel by activityViewModels()

    companion object {
        private const val MAX_ALLOWED_MOBILE_NUM_LEN = 10

        @JvmStatic
        fun newInstance() = UserDetailsEntryFragment()
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentUserDetailsEntryBinding = DataBindingUtil.inflate(inflater,
        R.layout.fragment_user_details_entry, container, false)

    override fun setUpView() {
        super.setUpView()
        permissionsBuilder(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS).build().send { result ->
            if (result.allGranted()) {
                //binding.etMobileNumber.isEnabled = true
            }
        }
    }

    override fun initListeners() {
        super.initListeners()
        binding.etMobileNumber.doOnTextChanged { text, _, _, _ ->
            binding.btnSubmitPhoneNum.isEnabled =
                text?.length == MAX_ALLOWED_MOBILE_NUM_LEN
        }
        binding.btnSubmitPhoneNum.setOnClickListener {
            hideKeyboard(requireContext())
            if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                permissionsBuilder(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS).build().send {}
                return@setOnClickListener
            }
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(requireContext())){
                activityViewModel.requestOverlayPermission()
            } else {
                //binding.etMobileNumber.isEnabled = false
                viewModel.saveUserMobile(binding.etMobileNumber.text.toString())
            }
        }
    }


    override fun observeViewModel() {
        super.observeViewModel()
        viewLifecycleOwner.apply {
            viewModel.onUserPhoneNumSaved.observe(this) {
                if(it == binding.etMobileNumber.text.toString()) {
                    findNavController().navigate(R.id.action_userDetailsEntryFragment_to_beneficiaryDetailsFragment)
                }
            }
        }
    }

    private fun hideKeyboard(context: Context) {
        if (view != null) {
            val imm: InputMethodManager =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.etMobileNumber.windowToken, 0)
        }
    }

}