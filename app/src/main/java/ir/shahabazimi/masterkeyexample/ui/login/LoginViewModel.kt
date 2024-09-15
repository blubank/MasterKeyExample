package ir.shahabazimi.masterkeyexample.ui.login

import android.content.Context
import android.util.Base64
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ir.shahabazimi.masterkeyexample.data.AuthenticateResultModel
import ir.shahabazimi.masterkeyexample.data.AuthenticateResultType
import ir.shahabazimi.masterkeyexample.utils.BiometricResult
import ir.shahabazimi.masterkeyexample.utils.Constants.BIOMETRIC_SAVED
import ir.shahabazimi.masterkeyexample.utils.Constants.PASSWORD_SAVED_KEY
import ir.shahabazimi.masterkeyexample.utils.Constants.USERNAME_SAVED_KEY
import ir.shahabazimi.masterkeyexample.utils.KeyStoreManager
import ir.shahabazimi.masterkeyexample.utils.PrefsHelper
import javax.crypto.Cipher

/**
 * @Author: Shahab Azimi
 * @Date: 2024 - 09 - 14
 **/
class LoginViewModel(
    private val prefsHelper: PrefsHelper,
    private val keyStoreManager: KeyStoreManager,
) : ViewModel() {


    private val _authenticateResult = MutableLiveData<AuthenticateResultModel>()
    val authenticateResult: LiveData<AuthenticateResultModel>
        get() = _authenticateResult


    fun usernameSaved() = prefsHelper.loadString(USERNAME_SAVED_KEY)


    fun isPasswordSaved(context: Context) =
        keyStoreManager.checkBiometricSupport(context) && prefsHelper.loadBoolean(BIOMETRIC_SAVED)


    fun saveUsername(username: String) = prefsHelper.saveString(USERNAME_SAVED_KEY, username)

    fun authenticate(context: FragmentActivity) {
        keyStoreManager.authenticate(context, object : BiometricResult {
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
                        val encryptedData = prefsHelper.loadString(PASSWORD_SAVED_KEY)
                        if (encryptedData != null) {
                            val password = keyStoreManager.decrypt(
                                cipher,
                                Base64.decode(encryptedData, Base64.DEFAULT)
                            )
                            _authenticateResult.postValue(
                                AuthenticateResultModel(AuthenticateResultType.SUCCESS, password)
                            )
                        } else {
                            _authenticateResult.postValue(
                                AuthenticateResultModel(AuthenticateResultType.ERROR, "Try Again")
                            )
                        }
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