package ir.shahabazimi.masterkeyexample

import android.content.Context
import android.content.SharedPreferences

/**
 * @Author: Shahab Azimi
 * @Date: 2024 - 09 - 14
 **/
class PrefsHelper private constructor(context: Context) {

    private val pref: SharedPreferences by lazy {
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    }


    fun saveString(key: String, value: String) = with(pref.edit()) {
        putString(key, value)
        apply()
    }


    fun loadString(key: String) = pref.getString(key, null)

    fun saveBoolean(key: String, value: Boolean) = with(pref.edit()) {
        putBoolean(key, value)
        apply()
    }


    fun loadBoolean(key: String) = pref.getBoolean(key, false)


    companion object {
        private const val FILE_NAME: String = "master_key_example_secure_pref"

        @Volatile
        private var instance: PrefsHelper? = null
        operator fun invoke(context: Context): PrefsHelper =
            instance ?: synchronized(this) {
                instance ?: PrefsHelper(context).also { instance = it }
            }
    }
}
