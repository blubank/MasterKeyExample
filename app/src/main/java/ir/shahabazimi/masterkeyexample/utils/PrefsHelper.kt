package ir.shahabazimi.masterkeyexample.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * @Author: Shahab Azimi
 * @Date: 2024 - 09 - 14
 **/
class PrefsHelper private constructor(context: Context) {

    private val pref: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
    }

    fun save(key: String, value: Any) = with(pref.edit()) {
        when(value){
            is String -> putString(key,value)
            is Boolean -> putBoolean(key,value)
            is Int -> putInt(key,value)
            is Long -> putLong(key,value)
            is Float -> putFloat(key,value)
        }
        apply()
    }


    fun loadString(key: String) = pref.getString(key, null)

    fun loadBoolean(key: String) = pref.getBoolean(key, false)

    fun remove(key: String) = with(pref.edit()) {
        remove(key)
        apply()
    }

    companion object {
        private const val PREFS_FILE_NAME: String = "master_key_example_secure_pref"

        const val BIOMETRIC_SAVED = "biometric_saved"
        const val PASSWORD_SAVED_KEY = "password_saved_key"
        const val USERNAME_SAVED_KEY = "username_saved_key"

        @Volatile
        private var instance: PrefsHelper? = null
        operator fun invoke(context: Context): PrefsHelper =
            instance ?: synchronized(this) {
                instance ?: PrefsHelper(context).also { instance = it }
            }
    }
}
