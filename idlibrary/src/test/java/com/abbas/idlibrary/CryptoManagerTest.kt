package com.abbas.idlibrary

import com.abbas.idlibrary.utils.CryptoManager
import com.abbas.idlibrary.utils.IDThrowable
import com.abbas.idlibrary.utils.TestDispatcherProvider
import com.abbas.idlibrary.utils.assertThrows
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertArrayEquals
import org.junit.Before
import org.junit.Test
import javax.crypto.SecretKey

class CryptoManagerTest {

    private lateinit var cryptoManager: CryptoManager
    private lateinit var mockSecretKey: SecretKey
    private val dispatcherProvider = TestDispatcherProvider(StandardTestDispatcher())

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        // Mock the SecretKey
        mockSecretKey = createRealSecretKey()
        Dispatchers.setMain(dispatcherProvider.main())
        // Initialize CryptoManager with the mocked SecretKey
        cryptoManager = CryptoManager(secretKey = mockSecretKey, dispatcherProvider = dispatcherProvider)
    }

    @Test
    fun `test encryption and decryption`() = runTest {
        // Sample data
        val originalData = "Test Data".toByteArray()


        val encryptedData = cryptoManager.encryptedData(originalData)

        assertNotNull(encryptedData)
        assertTrue(encryptedData.size > originalData.size)

        val decryptedData = cryptoManager.loadEncryptedData(listOf(encryptedData)).first()
        assertArrayEquals(originalData, decryptedData)
    }

    @Test
    fun `test decryption with corrupted data`() = runTest {
        // Corrupted encrypted data (too short)
        val corruptedData = ByteArray(50)
        assertThrows<IDThrowable.CorruptedData> {
            cryptoManager.loadEncryptedData(listOf(corruptedData))
        }
    }

    // Helper method to create a real SecretKey for testing
    private fun createRealSecretKey(): SecretKey {
        val keyGen = javax.crypto.KeyGenerator.getInstance("AES")
        keyGen.init(256)
        return keyGen.generateKey()
    }
}