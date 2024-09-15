package ir.shahabazimi.masterkeyexample.utils

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import ir.shahabazimi.masterkeyexample.utils.Constants.ANDROID_KEY_STORE
import ir.shahabazimi.masterkeyexample.utils.Constants.KEY_ALIAS
import ir.shahabazimi.masterkeyexample.utils.Constants.KEY_SIZE
import ir.shahabazimi.masterkeyexample.utils.Constants.initialIV
import java.security.KeyStore
import java.util.concurrent.Executors
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * @Author: Shahab Azimi
 * @Date: 2024 - 09 - 14
 **/
class KeyStoreManager {

    private fun cryptoObject(
        mode: Int,
    ): BiometricPrompt.CryptoObject {
        return generateAndStoreKey().let { key ->
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            if (mode == Cipher.ENCRYPT_MODE)
                cipher.init(mode, key)
            else
                cipher.init(
                    mode, key, GCMParameterSpec(
                        128, initialIV()
                    )
                )
            BiometricPrompt.CryptoObject(cipher)
        }
    }

    private fun generateAndStoreKey(): SecretKey {
        val key = getKey()
        if (key != null) return key

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE
        )

        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).run {
            setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            setUserAuthenticationRequired(true)
            setKeySize(KEY_SIZE)
            setInvalidatedByBiometricEnrollment(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                setUserAuthenticationParameters(
                    0,
                    KeyProperties.AUTH_BIOMETRIC_STRONG
                )
            } else {
                setUserAuthenticationValidityDurationSeconds(15)
            }

        }.build()

        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }

    fun getKey(): SecretKey? {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore.load(null)
        return keyStore.getKey(KEY_ALIAS, null) as? SecretKey
    }

    fun deleteKey(): Boolean {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore.load(null)
        keyStore.deleteEntry(KEY_ALIAS)
        return true
    }

    fun authenticate(
        fragmentActivity: FragmentActivity,
        mode: Int,
        biometricListener: BiometricResult
    ) {
        val promptInfo = createPromptInfo()
        BiometricPrompt(
            fragmentActivity,
            Executors.newSingleThreadExecutor(),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    biometricListener.onError(errString.toString())
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val cipher = result.cryptoObject?.cipher
                    if (cipher == null)
                        biometricListener.onError("cipher is null")
                    else
                        biometricListener.onSuccess(cipher)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    biometricListener.onCancel()
                }
            }
        ).authenticate(promptInfo, cryptoObject(mode))
    }

    fun checkBiometricSupport(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    private fun createPromptInfo(): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
    }
}