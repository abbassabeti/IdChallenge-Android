package com.abbas.idlibrary.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
 * Singleton class that manages the generation and retrieval of a symmetric AES key using Android Keystore.
 */
object SymmetricKeyProvider {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "idVerify.symmetrickey"

    fun generateSymmetricKey(): SecretKey {
        try {

            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )

            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            val value = keyGenerator.generateKey()
            return value
        } catch (e: Exception) {
            throw IDThrowable.SecretKeyGenerationException
        }
    }

    fun retrieveSymmetricKey(): SecretKey {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
                load(null)
            }

            // Check if the key already exists
            keyStore.getKey(KEY_ALIAS, null)?.let {
                return it as SecretKey
            }

            // If key does not exist, generate a new one
            return generateSymmetricKey()
        } catch (e: Exception) {
            throw IDThrowable.SecretKeyRetrievalException
        }
    }

    fun deleteSymmetricKey() {
        try {
            // Initialize KeyStore instance for Android Keystore
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
                load(null)
            }

            // Delete the key entry using the alias
            keyStore.deleteEntry(KEY_ALIAS)
        } catch (e: Exception) {
            throw IDThrowable.SecretKeyRetrievalException
        }
    }
}