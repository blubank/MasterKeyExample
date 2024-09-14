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
        keyStoreManager.checkBiometricSupport(context) && prefsHelper.loadBoolean(PASSWORD_SAVED_KEY)


    fun authenticate(context: FragmentActivity) {
        keyStoreManager.authenticate(context, object : BiometricResult {
            override fun onError(error: String) {
                _authenticateResult.value =
                    AuthenticateResultModel(AuthenticateResultType.ERROR, error)
            }

            override fun onCancel() {
                _authenticateResult.value = AuthenticateResultModel(
                    AuthenticateResultType.CANCELED,
                    "Authentication cancelled"
                )
            }

            override fun onSuccess(cipher: Cipher?) {
                if (cipher != null) {
                    try {
                        cipher.init(
                            Cipher.DECRYPT_MODE,
                            keyStoreManager.getKey()
                        )
                        val decryptedData = cipher.doFinal(
                            prefsHelper.loadString(PASSWORD_SAVED_KEY)?.toByteArray()
                        )
                        val password = Base64.encodeToString(decryptedData, Base64.DEFAULT)

                        _authenticateResult.value =
                            AuthenticateResultModel(AuthenticateResultType.SUCCESS, password)
                    } catch (e: Exception) {
                        _authenticateResult.value =
                            AuthenticateResultModel(AuthenticateResultType.ERROR, "Try Again")

                    }
                } else {
                    keyStoreManager.deleteKey()
                    prefsHelper.remove(PASSWORD_SAVED_KEY)
                    _authenticateResult.value =
                        AuthenticateResultModel(AuthenticateResultType.REMOVED_KEY, "Key Removed")
                }
            }

        })


    }

}