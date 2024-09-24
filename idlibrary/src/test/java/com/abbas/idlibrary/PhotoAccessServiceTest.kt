package com.abbas.idlibrary


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.abbas.idlibrary.usecases.PhotoAccessServiceImpl
import com.abbas.idlibrary.usecases.StorageService
import com.abbas.idlibrary.utils.CryptoManager
import com.abbas.idlibrary.utils.assertThrows
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

class PhotoAccessServiceTest {

    private lateinit var storageService: StorageService
    private lateinit var cryptoManager: CryptoManager
    private lateinit var photoAccessService: PhotoAccessServiceImpl
    private lateinit var bitmapFactoryMockedStatic: MockedStatic<BitmapFactory>

    @Before
    fun setUp() {
        storageService = mock(StorageService::class.java)
        cryptoManager = mock(CryptoManager::class.java)
        photoAccessService = PhotoAccessServiceImpl(storageService, cryptoManager)
        bitmapFactoryMockedStatic = Mockito.mockStatic(BitmapFactory::class.java)
        bitmapFactoryMockedStatic.`when`<Bitmap> {
            BitmapFactory.decodeByteArray(Mockito.any(ByteArray::class.java), Mockito.anyInt(), Mockito.anyInt())
        }.thenReturn(mock(Bitmap::class.java))
    }

    @After
    fun tearDown() {
        bitmapFactoryMockedStatic.close()
        Mockito.framework().clearInlineMocks()
    }

    @Test
    fun `test saveImage success`() = runTest {
        val fileName = "test_photo.enc"
        val data = byteArrayOf(1, 2, 3, 4, 5)
        val encryptedData = byteArrayOf(10, 20, 30, 40, 50)

        // Mock encryption
        whenever(cryptoManager.encryptedData(data)).thenReturn(encryptedData)

        // Mock storage save success
        whenever(storageService.saveEncryptedImage(fileName, encryptedData)).thenReturn(true)

        val result = photoAccessService.saveImage(fileName, data)

        assertTrue(result)

        verify(cryptoManager, times(1)).encryptedData(data)
        verify(storageService, times(1)).saveEncryptedImage(fileName, encryptedData)
    }

    @Test
    fun `test saveImage storage failure`() = runTest {
        val fileName = "test_photo.enc"
        val data = byteArrayOf(1, 2, 3, 4, 5)
        val encryptedData = byteArrayOf(10, 20, 30, 40, 50)

        // Mock encryption
        whenever(cryptoManager.encryptedData(data)).thenReturn(encryptedData)

        // Mock storage save failure
        whenever(storageService.saveEncryptedImage(fileName, encryptedData)).thenReturn(false)

        val result = photoAccessService.saveImage(fileName, data)

        assertFalse(result)

        verify(cryptoManager, times(1)).encryptedData(data)
        verify(storageService, times(1)).saveEncryptedImage(fileName, encryptedData)
    }

    @Test
    fun `test accessPhotos success`() = runTest {
        val encryptedPhotos = listOf(byteArrayOf(10, 20, 30), byteArrayOf(40, 50, 60))
        val decryptedPhotos = listOf(byteArrayOf(1, 2, 3), byteArrayOf(4, 5, 6))

        // Mock retrieval of encrypted photos
        whenever(storageService.retrieveAllEncryptedImages()).thenReturn(encryptedPhotos)

        // Mock decryption of each photo
        whenever(cryptoManager.loadEncryptedData(encryptedPhotos)).thenReturn(decryptedPhotos)

        val result = photoAccessService.accessPhotos()

        assertEquals(2, result.size)

        verify(storageService, times(1)).retrieveAllEncryptedImages()
        verify(cryptoManager, times(1)).loadEncryptedData(encryptedPhotos)
    }

    @Test
    fun `test accessPhotos decryption failure`() = runTest {
        val encryptedPhotos = listOf(byteArrayOf(10, 20, 30), byteArrayOf(40, 50, 60))
        whenever(storageService.retrieveAllEncryptedImages()).thenReturn(encryptedPhotos)
        whenever(cryptoManager.loadEncryptedData(encryptedPhotos)).thenThrow(RuntimeException(""))

        assertThrows<RuntimeException> {
            photoAccessService.accessPhotos()
        }
    }
}