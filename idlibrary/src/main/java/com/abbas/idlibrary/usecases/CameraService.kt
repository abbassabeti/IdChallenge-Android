package com.abbas.idlibrary.usecases

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.abbas.idlibrary.utils.Either
import com.abbas.idlibrary.utils.IDError
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume

interface CameraService {
    suspend fun capturePhoto(): Either<ByteArray, IDError>
}

class CameraServiceImpl constructor(
    private val context: Context,
    lifecycleOwner: LifecycleOwner,
    activityResultRegistry: ActivityResultRegistry
): CameraService {

    private var onResult: ((Either<ByteArray, IDError>) -> Unit)? = null

    init {
        setupCameraLauncher(lifecycleOwner, activityResultRegistry)
    }

    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    // Method to capture a photo with camera permission check
    override suspend fun capturePhoto(): Either<ByteArray, IDError> {
        return suspendCancellableCoroutine { continuation ->
            onResult = { result ->
                continuation.resume(result)
            }
            if (isCameraPermissionGranted()) {
                launchCamera()
            } else {
                continuation.resume(Either.Failure(IDError.PermissionsNotProvided))
            }
        }

    }

    private fun setupCameraLauncher(
        lifecycleOwner: LifecycleOwner,
        activityResultRegistry: ActivityResultRegistry,
    ) {
        cameraLauncher = activityResultRegistry.register(
            "camera_key",
            lifecycleOwner,
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val photo = result.data?.extras?.get("data") as Bitmap
                val photoData: ByteArray = bitmapToByteArray(photo)
                onResult?.invoke(Either.Success(photoData))
            } else {
                onResult?.invoke(Either.Failure(IDError.FailedInCapture))
            }
        }
    }

    // Check if the camera permission is granted
    private fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    // Launch the camera intent
    private fun launchCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    // Helper function to convert Bitmap to ByteArray
    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        return stream.toByteArray()
    }
}