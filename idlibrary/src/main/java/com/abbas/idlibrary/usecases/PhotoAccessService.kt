package com.abbas.idlibrary.usecases

import android.graphics.Bitmap
import javax.inject.Inject
import android.graphics.BitmapFactory
import com.abbas.idlibrary.utils.CryptoManager

interface PhotoAccessService {
    suspend fun saveImage(fileName: String, data: ByteArray): Boolean
    suspend fun accessPhotos(): List<Bitmap>
}

class PhotoAccessServiceImpl @Inject constructor(
    private val storageService: StorageService,
    private val cryptoManager: CryptoManager
) : PhotoAccessService {

    override suspend fun saveImage(fileName: String, data: ByteArray): Boolean {
        val encryptedData = cryptoManager.encryptedData(data)
        return storageService.saveEncryptedImage(fileName, encryptedData)
    }
    override suspend fun accessPhotos(): List<Bitmap> {
        val encryptedPhotos = storageService.retrieveAllEncryptedImages()
        val decryptedPhotos = cryptoManager.loadEncryptedData(items = encryptedPhotos)
        return decryptedPhotos.map { it.convertToBitmap() }
    }
}

fun ByteArray.convertToBitmap(): Bitmap {
    return BitmapFactory.decodeByteArray(this, 0, this.size)
}