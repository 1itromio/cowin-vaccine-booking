package dev.romio.cowinvaccinebook.viewmodel

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel

abstract class BaseViewModel: ViewModel() {

    @CallSuper
    open fun onViewCreate() {
        // Do Nothing
    }

    @CallSuper
    open fun onViewStart() {

    }

    @CallSuper
    open fun onViewFinish() {
        // Do Nothing
    }
}