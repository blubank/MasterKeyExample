package ir.shahabazimi.masterkeyexample

import android.util.Base64
import androidx.test.ext.junit.runners.AndroidJUnit4
import ir.shahabazimi.masterkeyexample.utils.KeyStoreManager
import ir.shahabazimi.masterkeyexample.utils.KeyStoreManager.Companion.ALGORITHM
import ir.shahabazimi.masterkeyexample.utils.KeyStoreManager.Companion.OAEPParams
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import javax.crypto.Cipher


@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test
    fun encryptAndDecryptTest() {
        val keyStoreManager = KeyStoreManager()
        val encryptedData = keyStoreManager.encrypt("Shahab")
        val encryptedDataString = Base64.encodeToString(encryptedData, Base64.DEFAULT)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, keyStoreManager.generateOrGetPrivateKey(), OAEPParams)

        val decryptedData = keyStoreManager.decrypt(
            cipher, Base64.decode(encryptedDataString, Base64.DEFAULT)
        )
        assertEquals("Shahab", decryptedData)

    }
}