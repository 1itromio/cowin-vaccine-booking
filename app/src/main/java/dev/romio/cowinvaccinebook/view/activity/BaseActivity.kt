package dev.romio.cowinvaccinebook.view.activity

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import dev.romio.cowinvaccinebook.viewmodel.BaseViewModel

abstract class BaseActivity<VM: BaseViewModel>: AppCompatActivity() {

    abstract val viewModel: VM

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        viewModel.onViewCreate()
    }

    override fun onStart() {
        super.onStart()
        viewModel.onViewStart()
    }


    override fun onDestroy() {
        viewModel.onViewFinish()
        super.onDestroy()
    }
}