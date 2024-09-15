package ir.shahabazimi.masterkeyexample.utils

import java.nio.charset.StandardCharsets

/**
 * @Author: Shahab Azimi
 * @Date: 2024 - 09 - 14
 **/
object Constants {

    const val PREFS_FILE_NAME: String = "master_key_example_secure_pref"

    const val ANDROID_KEY_STORE = "AndroidKeyStore"
    const val KEY_SIZE = 256
    const val KEY_ALIAS = "MasterKeyExampleAlias"

    const val BIOMETRIC_SAVED = "biometric_saved"
    const val PASSWORD_SAVED_KEY = "password_saved_key"
    const val USERNAME_SAVED_KEY = "username_saved_key"


    fun initialIV() =
        PREFS_FILE_NAME.toByteArray(StandardCharsets.UTF_8).copyOfRange(0, 12)

}