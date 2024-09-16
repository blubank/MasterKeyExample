package ir.shahabazimi.masterkeyexample.utils

import ir.shahabazimi.masterkeyexample.data.AuthenticateErrorType
import javax.crypto.Cipher

/**
 * @Author: Shahab Azimi
 * @Date: 2024 - 09 - 14
 **/
interface BiometricResult {
    fun onError(type: AuthenticateErrorType, error: String? = "")
    fun onCancel()
    fun onSuccess(cipher: Cipher)
}