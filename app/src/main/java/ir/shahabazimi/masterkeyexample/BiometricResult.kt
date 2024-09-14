package ir.shahabazimi.masterkeyexample

import javax.crypto.Cipher

interface BiometricResult {
    fun onError(error: String)
    fun onCancel()
    fun onSuccess(cipher: Cipher?)
}