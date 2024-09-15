package ir.shahabazimi.masterkeyexample.ui.fingerprint

import android.util.Base64
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.shahabazimi.masterkeyexample.data.AuthenticateResultModel
import ir.shahabazimi.masterkeyexample.data.AuthenticateResultType
import ir.shahabazimi.masterkeyexample.utils.BiometricResult
import ir.shahabazimi.masterkeyexample.utils.Constants.BIOMETRIC_SAVED
import ir.shahabazimi.masterkeyexample.utils.Constants.PASSWORD_SAVED_KEY
import ir.shahabazimi.masterkeyexample.utils.Constants.initialIV
import ir.shahabazimi.masterkeyexample.utils.KeyStoreManager
import ir.shahabazimi.masterkeyexample.utils.PrefsHelper
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher

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


    fun authenticate(context: FragmentActivity, password: String) = viewModelScope.launch {
        keyStoreManager.authenticate(context, Cipher.ENCRYPT_MODE, object : BiometricResult {
            override fun onError(error: String) {
                _authenticateResult.postValue(
                    AuthenticateResultModel(AuthenticateResultType.ERROR, error)
                )
            }

            override fun onCancel() {
                _authenticateResult.postValue(
                    AuthenticateResultModel(
                        AuthenticateResultType.CANCELED,
                        "Authentication cancelled"
                    )
                )
            }

            override fun onSuccess(cipher: Cipher?) {
                if (cipher != null) {
                    try {
                        val dataToEncrypt = password.toByteArray(StandardCharsets.UTF_8)
                        val encryptedData = cipher.doFinal(dataToEncrypt)
                        val encryptedPassword = String(encryptedData, StandardCharsets.UTF_8)

                        prefsHelper.saveString(PASSWORD_SAVED_KEY, encryptedPassword)
                        prefsHelper.saveBoolean(BIOMETRIC_SAVED, true)

                        _authenticateResult.postValue(
                            AuthenticateResultModel(
                                AuthenticateResultType.SUCCESS,
                                encryptedPassword
                            )
                        )
                    } catch (e: Exception) {
                        _authenticateResult.postValue(
                            AuthenticateResultModel(AuthenticateResultType.ERROR, "Try Again")
                        )

                    }
                } else {
                    keyStoreManager.deleteKey()
                    prefsHelper.remove(PASSWORD_SAVED_KEY)
                    _authenticateResult.postValue(
                        AuthenticateResultModel(AuthenticateResultType.REMOVED_KEY, "Key Removed")
                    )
                }
            }

        })
    }


}

