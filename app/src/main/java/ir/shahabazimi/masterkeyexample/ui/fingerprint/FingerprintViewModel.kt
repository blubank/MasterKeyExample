package ir.shahabazimi.masterkeyexample.ui.fingerprint

import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.shahabazimi.masterkeyexample.data.AuthenticateResultModel
import ir.shahabazimi.masterkeyexample.data.AuthenticateResultType
import ir.shahabazimi.masterkeyexample.utils.Constants.BIOMETRIC_SAVED
import ir.shahabazimi.masterkeyexample.utils.Constants.PASSWORD_SAVED_KEY
import ir.shahabazimi.masterkeyexample.utils.KeyStoreManager
import ir.shahabazimi.masterkeyexample.utils.PrefsHelper
import kotlinx.coroutines.launch

/**
 * @Author: Shahab Azimi
 * @Date: 2024 - 09 - 14
 **/
class FingerprintViewModel(
    private val prefsHelper: PrefsHelper,
    private val keyStoreManager: KeyStoreManager,
) : ViewModel() {

    private val _authenticateResult = MutableLiveData<AuthenticateResultModel>()
    val authenticateResult: LiveData<AuthenticateResultModel>
        get() = _authenticateResult


    fun authenticate(password: String) = viewModelScope.launch {
        val encryptedPassword = keyStoreManager.encrypt(password)
        if (encryptedPassword.isEmpty()) {
            _authenticateResult.postValue(
                AuthenticateResultModel(AuthenticateResultType.ERROR, "Try Again")
            )
        } else {
            prefsHelper.saveString(
                PASSWORD_SAVED_KEY,
                Base64.encodeToString(encryptedPassword, Base64.DEFAULT)
            )
            prefsHelper.saveBoolean(BIOMETRIC_SAVED, true)

            _authenticateResult.postValue(
                AuthenticateResultModel(
                    AuthenticateResultType.SUCCESS,
                    Base64.encodeToString(encryptedPassword, Base64.DEFAULT)
                )
            )
        }
    }


}

