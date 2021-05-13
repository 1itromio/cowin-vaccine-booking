package dev.romio.cowinvaccinebook.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.romio.cowinvaccinebook.R
import dev.romio.cowinvaccinebook.databinding.FragmentSplashBinding
import dev.romio.cowinvaccinebook.viewmodel.SplashFragmentViewModel

@AndroidEntryPoint
class SplashFragment : BaseFragment<SplashFragmentViewModel, FragmentSplashBinding>() {

    companion object {
        @JvmStatic
        fun newInstance() = SplashFragment()
    }

    override val viewModel: SplashFragmentViewModel by viewModels()

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSplashBinding = DataBindingUtil.inflate(inflater,
        R.layout.fragment_splash, container, false)


    override fun observeViewModel() {
        super.observeViewModel()
        viewLifecycleOwner.apply {
            viewModel.onOpenScreen.observe(this) {
                when(it) {
                    SplashFragmentViewModel.USER_DETAILS_ENTRY -> findNavController()
                        .navigate(R.id.action_splashFragment_to_userDetailsEntryFragment)
                    SplashFragmentViewModel.BENEFICIARY_SCREEN -> findNavController()
                        .navigate(R.id.action_splashFragment_to_beneficiaryDetailsFragment)
                }
            }
        }
    }


}