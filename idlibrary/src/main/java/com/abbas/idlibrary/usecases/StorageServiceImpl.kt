package com.abbas.idlibrary.usecases

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

interface StorageService {
    fun saveEncryptedImage(fileName: String, data: ByteArray): Boolean
    fun retrieveAllEncryptedImages(): List<ByteArray>
}

class StorageServiceImpl(private val context: Context) : StorageService {

    private val imageDirectoryName = "encrypted_images"

    override fun saveEncryptedImage(fileName: String, data: ByteArray): Boolean {
        return try {
            val imageDirectory = getImageDirectory()
            val file = File(imageDirectory, fileName)
            val fos = FileOutputStream(file)
            fos.write(data)
            fos.close()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    override fun retrieveAllEncryptedImages(): List<ByteArray> {
        val imageDirectory = getImageDirectory()
        val imageFiles = imageDirectory.listFiles()

        return imageFiles?.mapNotNull { file ->
            try {
                file.readBytes()
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        } ?: emptyList()
    }

    private fun getImageDirectory(): File {
        val imageDirectory = File(context.filesDir, imageDirectoryName)
        if (!imageDirectory.exists()) {
            imageDirectory.mkdir()
        }
        return imageDirectory
    }
}