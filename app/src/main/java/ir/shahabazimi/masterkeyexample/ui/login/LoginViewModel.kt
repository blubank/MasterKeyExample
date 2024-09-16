package ir.shahabazimi.masterkeyexample.ui.login

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ir.shahabazimi.masterkeyexample.data.AuthenticateErrorType
import ir.shahabazimi.masterkeyexample.data.AuthenticateResultModel
import ir.shahabazimi.masterkeyexample.data.AuthenticateResultType
import ir.shahabazimi.masterkeyexample.utils.BiometricResult
import ir.shahabazimi.masterkeyexample.utils.KeyStoreManager
import ir.shahabazimi.masterkeyexample.utils.PrefsHelper
import ir.shahabazimi.masterkeyexample.utils.PrefsHelper.Companion.BIOMETRIC_SAVED
import ir.shahabazimi.masterkeyexample.utils.PrefsHelper.Companion.PASSWORD_SAVED_KEY
import ir.shahabazimi.masterkeyexample.utils.PrefsHelper.Companion.USERNAME_SAVED_KEY
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
        keyStoreManager.checkBiometricSupport(context) &&
                prefsHelper.loadBoolean(BIOMETRIC_SAVED) &&
                !prefsHelper.loadString(PASSWORD_SAVED_KEY).isNullOrEmpty()


    fun saveUsername(username: String) = prefsHelper.save(USERNAME_SAVED_KEY, username)

    fun authenticate(context: FragmentActivity) {
        keyStoreManager.authenticate(context, object : BiometricResult {
            override fun onError(type: AuthenticateErrorType, error: String?) {
                if (type == AuthenticateErrorType.REMOVE_KEY) {
                    prefsHelper.remove(PASSWORD_SAVED_KEY)
                    prefsHelper.remove(BIOMETRIC_SAVED)
                    _authenticateResult.postValue(
                        AuthenticateResultModel(AuthenticateResultType.REMOVE_KEY, error.orEmpty())
                    )
                } else
                    _authenticateResult.postValue(
                        AuthenticateResultModel(AuthenticateResultType.ERROR, error.orEmpty())
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

            override fun onSuccess(cipher: Cipher) {
                try {
                    val encryptedData = prefsHelper.loadString(PASSWORD_SAVED_KEY)
                    if (encryptedData != null) {
                        val password = keyStoreManager.decrypt(
                            cipher,
                            encryptedData
                        )
                        if (password.isNullOrEmpty())
                            _authenticateResult.postValue(
                                AuthenticateResultModel(AuthenticateResultType.ERROR, "Try Again")
                            )
                        else
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
                        AuthenticateResultModel(AuthenticateResultType.REMOVE_KEY, "Key Removed")
                    )

                }
            }

        })


    }

}