package dev.romio.cowinvaccinebook.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.romio.cowinvaccinebook.repository.CowinAppRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserDetailsEntryViewModel @Inject constructor(
    private val cowinAppRepository: CowinAppRepository
): BaseViewModel() {

    private val _onUserPhoneNumSaved = MutableLiveData<String>()

    val onUserPhoneNumSaved: LiveData<String> = _onUserPhoneNumSaved

    fun saveUserMobile(mobile: String) {
        cowinAppRepository.saveMobileNum(mobile)
        viewModelScope.launch {
            delay(100)
            cowinAppRepository.getSavedMobileNum()?.let {
                _onUserPhoneNumSaved.postValue(it)
            }
        }
    }
}