package ir.shahabazimi.masterkeyexample.utils

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import java.nio.charset.StandardCharsets
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.util.concurrent.Executors
import javax.crypto.Cipher


/**
 * @Author: Shahab Azimi
 * @Date: 2024 - 09 - 14
 **/
class KeyStoreManager {

    companion object {
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        private const val ALGORITHM = "RSA/ECB/PKCS1Padding"
        private const val KEY_SIZE = 2048
        private const val KEY_ALIAS = "master_key_example_alias"
    }

    private fun cryptoObject(
    ): BiometricPrompt.CryptoObject {
        return generateOrGetPrivateKey().let { key ->
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, key)
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
            setBlockModes(KeyProperties.BLOCK_MODE_ECB)
            setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
            setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
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
            cipher.init(Cipher.ENCRYPT_MODE, key)
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
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore.load(null)
        keyStore.deleteEntry(KEY_ALIAS)
        return true
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
        )
        prompt.authenticate(promptInfo, cryptoObject())

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