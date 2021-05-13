package dev.romio.cowinvaccinebook.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import dev.romio.cowinvaccinebook.viewmodel.BaseViewModel

abstract class BaseFragment<VM: BaseViewModel, DB: ViewDataBinding>: Fragment() {

    protected abstract val viewModel: VM
    protected lateinit var binding: DB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = getViewBinding(inflater, container)
        viewModel.onViewCreate()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpView()
        observeViewModel()
        initListeners()
    }

    override fun onDestroyView() {
        viewModel.onViewFinish()
        super.onDestroyView()
    }

    @CallSuper
    open fun observeViewModel() {
        // Do Nothing
    }

    @CallSuper
    open fun setUpView() {

    }

    @CallSuper
    open fun initListeners() {

    }

    abstract fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): DB
}