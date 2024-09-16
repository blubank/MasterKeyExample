package ir.shahabazimi.masterkeyexample.di

import ir.shahabazimi.masterkeyexample.ui.fingerprint.FingerprintViewModel
import ir.shahabazimi.masterkeyexample.ui.login.LoginViewModel
import ir.shahabazimi.masterkeyexample.utils.KeyStoreManager
import ir.shahabazimi.masterkeyexample.utils.PrefsHelper
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * @Author: Shahab Azimi
 * @Date: 2024 - 09 - 14
 **/
val appModule = module {

    single {
        PrefsHelper(context = get())
    }

    single {
        KeyStoreManager
    }

    viewModel {
        LoginViewModel(get(), get())
    }

    viewModel {
        FingerprintViewModel(get(), get())
    }

}