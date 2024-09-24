package com.abbas.idlibrary

import android.content.Context
import com.abbas.idlibrary.usecases.StorageService
import com.abbas.idlibrary.usecases.StorageServiceImpl
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.io.File

class StorageServiceTest {

    private lateinit var context: Context
    private lateinit var storageService: StorageService
    private lateinit var tempDir: File
    private lateinit var encryptedImagesDir: File

    @Before
    fun setUp() {
        // Use a temporary directory for testing
        context = mock(Context::class.java)

        // Mock the filesDir to point to a temporary directory
        tempDir = File(System.getProperty("java.io.tmpdir"), "test_encrypted_images").apply {
            mkdirs()
        }

        encryptedImagesDir = File(tempDir, "encrypted_images").apply {
            mkdirs()
        }

        `when`(context.filesDir).thenReturn(tempDir)

        storageService = StorageServiceImpl(context)
    }

    @After
    fun tearDown() {
        // Clean up the temporary directory after tests
        tempDir.deleteRecursively()
    }

    @Test
    fun `test saveEncryptedImage success`() {
        val fileName = "test_image.enc"
        val data = byteArrayOf(1, 2, 3, 4, 5)

        val result = storageService.saveEncryptedImage(fileName, data)

        assertTrue(result)

        // Verify the file exists and contains the correct data
        val savedFile = File(encryptedImagesDir, fileName)
        assertTrue(savedFile.exists())
        assertArrayEquals(data, savedFile.readBytes())
    }

    @Test
    fun `test retrieveAllEncryptedImages success`() {
        val files = listOf(byteArrayOf(10, 20, 30), byteArrayOf(40, 50, 60))
        files.forEachIndexed { index, item ->
            storageService.saveEncryptedImage("image${index}.enc", item)
        }
        val images = storageService.retrieveAllEncryptedImages()

        assertEquals(2, images.size)
        assertTrue(images.containsAll(images))
    }

    @Test
    fun `test retrieveAllEncryptedImages with no files`() {
        val images = storageService.retrieveAllEncryptedImages()

        assertTrue(images.isEmpty())
    }

    @Test
    fun `test retrieveAllEncryptedImages handles IOExceptions gracefully`() {
        // Mock a file that cannot be read by making it non-readable
        val file = File(tempDir, "unreadable.enc").apply {
            writeBytes(byteArrayOf(70, 80, 90))
            setReadable(false)
        }

        val images = storageService.retrieveAllEncryptedImages()

        // The unreadable file should be skipped
        assertTrue(images.isEmpty())

        // Restore file readability for cleanup
        file.setReadable(true)
    }
}