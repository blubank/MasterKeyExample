package ir.shahabazimi.masterkeyexample.utils

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import ir.shahabazimi.masterkeyexample.data.AuthenticateErrorType
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
object KeyStoreManager {

    private const val ANDROID_KEY_STORE = "AndroidKeyStore"
    private const val ALGORITHM = "RSA/None/OAEPWithSHA-256AndMGF1Padding"
    private const val KEY_SIZE = 2048
    private const val KEY_ALIAS = "master_key_example_alias"
    private val OAEPParams = OAEPParameterSpec(
        "SHA-256",
        "MGF1",
        MGF1ParameterSpec.SHA256,
        PSource.PSpecified.DEFAULT
    )


    private fun getKeyStore(): KeyStore =
        KeyStore.getInstance(ANDROID_KEY_STORE).apply {
            load(null)
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

    private fun generateOrGetPrivateKey(): PrivateKey =
        getPrivateKey() ?: generateKeyPair().private


    private fun generateOrGetPublicKey(): PublicKey =
        getPublicKey() ?: generateKeyPair().public


    private fun getEncryptCipher(): Cipher =
        generateOrGetPublicKey().let { key ->
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, key, OAEPParams)
            cipher
        }


    private fun getPrivateKey() =
        getKeyStore().getKey(KEY_ALIAS, null) as? PrivateKey


    private fun getPublicKey() =
        getKeyStore().getCertificate(KEY_ALIAS)?.publicKey

    /**
     * The deleteKey function attempts to remove a specific key entry from the keystore.
     * It returns a Boolean value indicating whether the operation was successful.
     */
    fun deleteKey(): Boolean =
        try {
            getKeyStore().deleteEntry(KEY_ALIAS)
            true
        } catch (e: KeyStoreException) {
            false
        }

    /**
     * The encrypt function encrypts a given plain text string using a predefined cipher.
     * It converts the plain text into a byte array, encrypts it, and returns the encrypted data as a ByteArray.
     * If an error occurs during encryption, the function returns null.
     */
    fun encrypt(plainText: String): String? =
        try {
            val cipher = getEncryptCipher()
            Base64.encodeToString(cipher.doFinal(plainText.toByteArray()), Base64.DEFAULT)
        } catch (e: Exception) {
            null
        }

    /**
     * The decrypt function attempts to decrypt a given string (cipherText) using a specified Cipher object.
     * If successful, it returns the decrypted result as a String encoded in UTF-8.
     * If decryption fails, the function returns null.
     * This method is must be called after user successfully authenticated via authenticate() method
     */
    fun decrypt(cipher: Cipher, cipherText: String): String? =
        try {
            val decryptedBytes = cipher.doFinal(Base64.decode(cipherText, Base64.DEFAULT))
            String(decryptedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            null
        }

    /**
     * The checkBiometricSupport function determines whether the device supports and is ready to use biometric authentication (specifically strong biometric methods like fingerprint or face recognition).
     * It returns true if biometric authentication is supported and enrolled, and false otherwise.
     */
    fun checkBiometricSupport(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> false
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    /**
     * The authenticate function triggers biometric authentication in a given FragmentActivity, invoking callbacks to handle success, error, or failure scenarios.
     * It uses BiometricPrompt to present the biometric prompt to the user and process the result through a custom BiometricResult listener.
     */
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
                    biometricListener.onError(AuthenticateErrorType.TRY_AGAIN, errString.toString())
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val cipher = result.cryptoObject?.cipher
                    if (cipher == null) {
                        deleteKey()
                        biometricListener.onError(AuthenticateErrorType.REMOVE_KEY)
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
            biometricListener.onError(AuthenticateErrorType.REMOVE_KEY)
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