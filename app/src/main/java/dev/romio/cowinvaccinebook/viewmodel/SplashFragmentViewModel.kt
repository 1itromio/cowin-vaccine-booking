package dev.romio.cowinvaccinebook.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.romio.cowinvaccinebook.usecase.GetUserDetailsUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashFragmentViewModel @Inject constructor(
    private val getUserDetailsUseCase: GetUserDetailsUseCase
): BaseViewModel() {

    companion object {
        private const val SPLASH_DELAY = 2000L
        const val USER_DETAILS_ENTRY = "USER_DETAILS_ENTRY"
        const val BENEFICIARY_SCREEN = "BENEFICIARY"
    }

    private val _onOpenScreen = MutableLiveData<String>()

    val onOpenScreen: LiveData<String> = _onOpenScreen

    override fun onViewCreate() {
        super.onViewCreate()
        viewModelScope.launch {
            delay(SPLASH_DELAY)
            val userDetails = getUserDetailsUseCase.execute(Unit)
            if(userDetails == null) {
                _onOpenScreen.postValue(USER_DETAILS_ENTRY)
            } else {
                _onOpenScreen.postValue(BENEFICIARY_SCREEN)
            }
        }
    }
}