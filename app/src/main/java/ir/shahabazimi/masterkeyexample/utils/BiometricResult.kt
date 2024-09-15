package ir.shahabazimi.masterkeyexample.utils

import javax.crypto.Cipher

/**
 * @Author: Shahab Azimi
 * @Date: 2024 - 09 - 14
 **/
interface BiometricResult {
    fun onError(error: String)
    fun onCancel()
    fun onSuccess(cipher: Cipher)
}