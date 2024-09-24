package com.abbas.idlibrary

import android.app.Activity
import android.graphics.Bitmap
import androidx.biometric.BiometricManager
import androidx.lifecycle.LifecycleOwner
import com.abbas.idlibrary.usecases.AuthenticationService
import com.abbas.idlibrary.usecases.AuthenticationServiceImpl
import com.abbas.idlibrary.usecases.CameraService
import com.abbas.idlibrary.usecases.CameraServiceImpl
import com.abbas.idlibrary.usecases.PhotoAccessService
import com.abbas.idlibrary.usecases.PhotoAccessServiceImpl
import com.abbas.idlibrary.usecases.StorageService
import com.abbas.idlibrary.usecases.StorageServiceImpl
import com.abbas.idlibrary.utils.BiometricInterface
import com.abbas.idlibrary.utils.BiometricPromptBuilder
import com.abbas.idlibrary.utils.BiometricPromptBuilderImpl
import com.abbas.idlibrary.utils.BiometricWrapper
import com.abbas.idlibrary.utils.CameraPermissionObserver
import com.abbas.idlibrary.utils.CryptoManager
import com.abbas.idlibrary.utils.DefaultDispatcherProvider
import com.abbas.idlibrary.utils.DispatcherProvider
import com.abbas.idlibrary.utils.Either
import com.abbas.idlibrary.utils.IDError
import com.abbas.idlibrary.utils.PermissionListener
import com.abbas.idlibrary.utils.SymmetricKeyProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume

interface IdVerification: PermissionListener {
    suspend fun takePhoto(): Either<Unit, IDError>
    suspend fun authenticateUser(activity: Activity): Either<Unit, IDError>
    suspend fun accessPhotos(activity: Activity): Either<List<Bitmap>, IDError>
}

object Id {
    fun initialize(owner: VerificationOwner): IdVerification {
        val dispatcherProvider = DefaultDispatcherProvider()
        return IdVerificationImpl(
            cameraService = createCameraService(owner),
            accessService = createPhotoStorageService(
                owner,
                dispatcherProvider = dispatcherProvider
            ),
            authService = createAuthService(
                BiometricPromptBuilderImpl(),
                BiometricWrapper(BiometricManager.from(owner.dependencies.context))
            ),
            cameraPermissionObserver = createCameraPermissionObserver(owner),
            lifecycleOwner = owner.dependencies.lifecycleOwner,
            dispatcherProvider = dispatcherProvider
        )
    }
}

internal class IdVerificationImpl(
    val cameraService: CameraService,
    val accessService: PhotoAccessService,
    val authService: AuthenticationService,
    val cameraPermissionObserver: CameraPermissionObserver,
    val dispatcherProvider: DispatcherProvider,
    lifecycleOwner: LifecycleOwner
): IdVerification {

    private val scope = CoroutineScope(dispatcherProvider.default())

    init {
        cameraPermissionObserver.permissionListener = this
        cameraPermissionObserver.onCreate()
        lifecycleOwner.lifecycle.addObserver(cameraPermissionObserver)
    }

    private var recentlyAuthorizedAccess: Boolean = false
    private var requiresCapture: Boolean = false
    private var onPermissionResult: ((Either<Unit, IDError>) -> Unit)? = null

    override suspend fun takePhoto(): Either<Unit, IDError> {
        return suspendCancellableCoroutine { continuation ->
            onPermissionResult = { result ->
                continuation.resume(result)
            }
            scope.launch {
                if (cameraPermissionObserver.checkPermission()) {
                    continuation.resume(capturePhoto())
                } else {
                    requiresCapture = true
                    cameraPermissionObserver.launch()
                }
            }
        }
    }

    // Authenticate the user using biometric authentication
    override suspend fun authenticateUser(activity: Activity): Either<Unit, IDError> {
        return when (val result = authService.authenticateUser(activity)) {
            is Either.Success -> {
                recentlyAuthorizedAccess = true
                scope.launch {
                    setAccessTimer()
                }
                Either.Success(Unit)
            }
            else -> {
                result
            }
        }
    }

    // Access stored encrypted photos
    override suspend fun accessPhotos(activity: Activity): Either<List<Bitmap>, IDError> {
        suspend fun loadPhotos(): Either.Success<List<Bitmap>> {
            val images = accessService.accessPhotos()
            return Either.Success(images)
        }
        return if (recentlyAuthorizedAccess) {
            loadPhotos()
        } else {
            when (val result = authenticateUser(activity)) {
                is Either.Success -> loadPhotos()
                else -> Either.Failure(result.getFailureOrNull() ?: IDError.AuthenticationFailed)
            }
        }
    }

    override fun permissionGranted() {
        if (requiresCapture) {
            requiresCapture = false
            CoroutineScope(dispatcherProvider.default()).launch {
                capturePhoto()
            }
        }
    }

    override fun permissionDenied() {
        onPermissionResult?.invoke(Either.Failure(IDError.PermissionsNotProvided))
    }

    private suspend fun capturePhoto(): Either<Unit, IDError> {
        when(val result = cameraService.capturePhoto()) {
            is Either.Failure -> return Either.Failure(IDError.FailedInCapture)
            is Either.Success -> {
                val encryptedData = result.getSuccessOrNull()
                if (encryptedData != null) {
                    val filename = generateTimestampedFilename()
                    val success = accessService.saveImage(filename, encryptedData)
                    return if (success) {
                        Either.Success(Unit)
                    } else {
                        Either.Failure(IDError.FailedInStoringPhotos)
                    }
                } else {
                    return Either.Failure(IDError.FailedInCapture)
                }
            }
        }
    }

    private suspend fun setAccessTimer() {
        delay(60000)
        clearImageAccess()
    }

    // Clear the access after the timeout
    private fun clearImageAccess() {
        recentlyAuthorizedAccess = false
    }

    // Utility method to generate a timestamped filename
    private fun generateTimestampedFilename(): String {
        val timestampFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val timestamp = timestampFormat.format(Date())
        return "photo_$timestamp.jpg"
    }
}

private fun createCameraService(owner: VerificationOwner): CameraService {
    return CameraServiceImpl(
        owner.dependencies.context,
        lifecycleOwner = owner.dependencies.lifecycleOwner,
        activityResultRegistry = owner.dependencies.activityResultRegistry
    )
}

private fun createStorageService(owner: VerificationOwner): StorageService {
    return StorageServiceImpl(
        context = owner.dependencies.context
    )
}

private fun createPhotoStorageService(owner: VerificationOwner, dispatcherProvider: DispatcherProvider): PhotoAccessService {
    return PhotoAccessServiceImpl(
        storageService = createStorageService(owner),
        cryptoManager = CryptoManager(
            SymmetricKeyProvider.retrieveSymmetricKey(),
            dispatcherProvider = dispatcherProvider
        )
    )
}

private fun createAuthService(
    biometricPromptBuilder: BiometricPromptBuilder,
    biometricPrompt: BiometricInterface
): AuthenticationService {
    return AuthenticationServiceImpl(
        biometricPromptBuilder,
        biometricPrompt
    )
}

private fun createCameraPermissionObserver(owner: VerificationOwner): CameraPermissionObserver {
    return CameraPermissionObserver(
        owner.dependencies.context
    )
}