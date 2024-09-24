package com.abbas.idlibrary.utils

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec


class CryptoManager(
    private val secretKey: SecretKey,
    private val dispatcherProvider: DispatcherProvider
) {

    // AES-GCM parameters
    private val cipherTransformation = "AES/GCM/NoPadding"
    private val nonceSize = 12 // 12 bytes for GCM nonce
    private val tagSize = 16   // 16 bytes for GCM authentication tag

    suspend fun encryptedData(data: ByteArray): ByteArray = withContext(dispatcherProvider.io()) {
        try {
            encrypt(data)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun loadEncryptedData(
        items: List<ByteArray>
    ): List<ByteArray> = withContext(dispatcherProvider.io()) {
        try {
            items.map { encryptedData ->
                async(dispatcherProvider.default()) {
                    decrypt(encryptedData)
                }
            }.awaitAll()
        } catch (e: AEADBadTagException) {
            throw IDThrowable.CorruptedData
        }
    }

    private fun encrypt(data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")

        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val iv = cipher.iv

        val encryptedBytes = cipher.doFinal(data)

        return iv + encryptedBytes
    }

    private fun decrypt(encryptedData: ByteArray): ByteArray {
        try {
            if (encryptedData.size < nonceSize + tagSize) {
                throw IDThrowable.CorruptedData
            }

            val iv = encryptedData.copyOfRange(0, nonceSize)
            val ciphertext = encryptedData.copyOfRange(nonceSize, encryptedData.size)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")

            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            return cipher.doFinal(ciphertext)
        } catch (_: Exception) {
            throw IDThrowable.CorruptedData
        }
    }
}