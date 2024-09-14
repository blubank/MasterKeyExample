package ir.shahabazimi.masterkeyexample

import android.util.Base64
import java.security.SecureRandom

/**
 * @Author: Shahab Azimi
 * @Date: 2024 - 09 - 14
 **/
class Utils {


    fun generate128BitKey(): String {
        val randomBytes = ByteArray(16)
        SecureRandom().nextBytes(randomBytes)
        val encodedBytes = Base64.encodeToString(randomBytes, Base64.URL_SAFE)
        return encodedBytes.trimEnd { it == '=' } // Remove padding manually
    }


}