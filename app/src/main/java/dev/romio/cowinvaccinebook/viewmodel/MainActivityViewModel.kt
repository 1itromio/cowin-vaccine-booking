package dev.romio.cowinvaccinebook.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.romio.cowinvaccinebook.repository.CowinAppRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val cowinAppRepository: CowinAppRepository
): BaseViewModel() {

    private val _requestOverlayPermission = MutableLiveData<Boolean>()
    private val _serviceRunning = MutableLiveData<Boolean>()

    val onRequestOverlayPermission: LiveData<Boolean> = _requestOverlayPermission
    val serviceRunning: LiveData<Boolean> = _serviceRunning

    override fun onViewStart() {
        super.onViewStart()
        viewModelScope.launch {
            cowinAppRepository.observeServiceRunningStatus().collect {
                _serviceRunning.postValue(it)
            }
        }
    }

    fun requestOverlayPermission() {
        _requestOverlayPermission.postValue(true)
    }
}