package ir.shahabazimi.masterkeyexample.utils

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import ir.shahabazimi.masterkeyexample.data.AuthenticateResultType
import java.nio.charset.StandardCharsets
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.MGF1ParameterSpec
import java.util.concurrent.Executors
import javax.crypto.Cipher
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource


/**
 * @Author: Shahab Azimi
 * @Date: 2024 - 09 - 14
 **/
class KeyStoreManager {

    companion object {
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val ALGORITHM = "RSA/None/OAEPWithSHA-256AndMGF1Padding"
        private const val KEY_SIZE = 2048
        private const val KEY_ALIAS = "master_key_example_alias"
        val OAEPParams = OAEPParameterSpec(
            "SHA-256",
            "MGF1",
            MGF1ParameterSpec.SHA256,
            PSource.PSpecified.DEFAULT
        )
    }

    private fun cryptoObject(
    ): BiometricPrompt.CryptoObject {
        return generateOrGetPrivateKey().let { key ->
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, key, OAEPParams)
            BiometricPrompt.CryptoObject(cipher)
        }
    }


    private fun generateKeyPair(): KeyPair {
        val keyGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEY_STORE
        )
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).run {
            setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
            setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PSS)
            setUserAuthenticationRequired(true)
            setInvalidatedByBiometricEnrollment(true)
            setKeySize(KEY_SIZE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                setUserAuthenticationParameters(
                    0,
                    KeyProperties.AUTH_BIOMETRIC_STRONG
                )
            } else {
                setUserAuthenticationValidityDurationSeconds(15)
            }

        }.build()

        keyGenerator.initialize(keyGenParameterSpec)
        return keyGenerator.generateKeyPair()
    }

    fun generateOrGetPrivateKey(): PrivateKey {
        return getPrivateKey() ?: generateKeyPair().private
    }

    private fun generateOrGetPublicKey(): PublicKey {
        return getPublicKey() ?: generateKeyPair().public
    }

    private fun getEncryptCipher(): Cipher {
        return generateOrGetPublicKey().let { key ->
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, key, OAEPParams)
            cipher
        }
    }

    private fun getPrivateKey(): PrivateKey? {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore.load(null)
        return keyStore.getKey(KEY_ALIAS, null) as? PrivateKey
    }

    private fun getPublicKey(): PublicKey? {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore.load(null)
        return keyStore.getCertificate(KEY_ALIAS)?.publicKey
    }

    fun deleteKey(): Boolean {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
            keyStore.load(null)
            keyStore.deleteEntry(KEY_ALIAS)
            return true
        } catch (e: KeyStoreException) {
            return false
        }
    }

    fun encrypt(plainText: String): ByteArray {
        val cipher = getEncryptCipher()
        return cipher.doFinal(plainText.toByteArray())
    }

    fun decrypt(cipher: Cipher, cipherText: ByteArray): String {
        val decryptedBytes = cipher.doFinal(cipherText)
        return String(decryptedBytes, StandardCharsets.UTF_8)
    }

    fun authenticate(
        fragmentActivity: FragmentActivity,
        biometricListener: BiometricResult
    ) {
        val promptInfo = createPromptInfo()
        val prompt = BiometricPrompt(
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
                    if (cipher == null) {
                        deleteKey()
                        biometricListener.onError(AuthenticateResultType.REMOVED_KEY.name)
                    } else
                        biometricListener.onSuccess(cipher)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    biometricListener.onCancel()
                }
            }
        )
        try {
            prompt.authenticate(promptInfo, cryptoObject())
        } catch (e: KeyPermanentlyInvalidatedException) {
            deleteKey()
            biometricListener.onError(AuthenticateResultType.REMOVED_KEY.name)
        }

    }

    fun checkBiometricSupport(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> false
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