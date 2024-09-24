package com.abbas.idlibrary

import android.app.Activity
import android.graphics.Bitmap
import androidx.biometric.BiometricManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.abbas.idlibrary.usecases.AuthenticationService
import com.abbas.idlibrary.usecases.CameraService
import com.abbas.idlibrary.usecases.PhotoAccessService
import com.abbas.idlibrary.utils.CameraPermissionObserver
import com.abbas.idlibrary.utils.DefaultDispatcherProvider
import com.abbas.idlibrary.utils.Either
import com.abbas.idlibrary.utils.IDError
import com.abbas.idlibrary.utils.TestDispatcherProvider
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub

class IdVerificationImplTest {

    private val mockCameraService: CameraService = mock()
    private val mockPhotoAccessService: PhotoAccessService = mock()
    private val mockAuthenticationService: AuthenticationService = mock()
    private val mockCameraPermissionObserver: CameraPermissionObserver = mock()
    private val mockLifecycleOwner: LifecycleOwner = mock()
    private val mockLifecycle: Lifecycle = mock()
    private val mockActivity: Activity = mock()
    private val mockBitmap: Bitmap = mock()

    private lateinit var idVerification: IdVerificationImpl
    private lateinit var idVerificationForTakePhoto: IdVerificationImpl

    private var biometricManagerMockedStatic: MockedStatic<BiometricManager>? = null

    private var dispatcherProvider = TestDispatcherProvider(StandardTestDispatcher())

    @Before
    fun setUp() {
        mockLifecycleOwner.stub {
            on { lifecycle } doReturn mockLifecycle
        }

        // Initialize IdVerificationImpl with mocked dependencies
        idVerification = IdVerificationImpl(
            cameraService = mockCameraService,
            accessService = mockPhotoAccessService,
            authService = mockAuthenticationService,
            cameraPermissionObserver = mockCameraPermissionObserver,
            dispatcherProvider = dispatcherProvider,
            lifecycleOwner = mockLifecycleOwner
        )

        idVerificationForTakePhoto = IdVerificationImpl(
            cameraService = mockCameraService,
            accessService = mockPhotoAccessService,
            authService = mockAuthenticationService,
            cameraPermissionObserver = mockCameraPermissionObserver,
            dispatcherProvider = DefaultDispatcherProvider(),
            lifecycleOwner = mockLifecycleOwner
        )
    }

    @After
    fun tearDown() {
        biometricManagerMockedStatic?.close()
        Mockito.framework().clearInlineMocks()
    }

    @Test
    fun `authenticateUser returns Success when authentication is successful`() = runTest {
        // Arrange
        mockAuthenticationService.stub {
            onBlocking { authenticateUser(any()) } doReturn Either.Success(Unit)
        }

        // Act
        val result = idVerification.authenticateUser(mockActivity)

        // Assert
        Assert.assertTrue(result is Either.Success)
        Assert.assertEquals(Unit, (result as Either.Success).value)
        Mockito.verify(mockAuthenticationService, Mockito.times(1)).authenticateUser(mockActivity)
    }

    @Test
    fun `authenticateUser returns Failure when authentication fails`() = runTest {
        // Arrange
        val error = IDError.AuthenticationFailed
        mockAuthenticationService.stub {
            onBlocking { authenticateUser(any()) } doReturn Either.Failure(error)
        }

        // Act
        val result = idVerification.authenticateUser(mockActivity)

        // Assert
        Assert.assertTrue(result is Either.Failure)
        Assert.assertEquals(error, (result as Either.Failure).value)
        Mockito.verify(mockAuthenticationService, Mockito.times(1)).authenticateUser(mockActivity)
    }

    @Test
    fun `accessPhotos authenticates user and returns list of Bitmaps on successful authentication`() = runTest {
        mockAuthenticationService.stub {
            onBlocking { authenticateUser(any()) } doReturn Either.Success(Unit)
        }

        mockPhotoAccessService.stub {
            onBlocking { accessPhotos() } doReturn listOf(mockBitmap)
        }

        // Act
        val result = idVerification.accessPhotos(mockActivity)

        // Assert
        Assert.assertTrue(result is Either.Success)
        Assert.assertEquals(listOf(mockBitmap), (result as Either.Success).value)
        Mockito.verify(mockAuthenticationService, Mockito.times(1)).authenticateUser(mockActivity)
        Mockito.verify(mockPhotoAccessService, Mockito.times(1)).accessPhotos()
    }

    @Test
    fun `accessPhotos returns Failure when authentication fails`() = runTest {

        mockAuthenticationService.stub {
            onBlocking {
                authenticateUser(any())
            } doReturn Either.Failure(IDError.AuthenticationFailed)
        }

        mockPhotoAccessService.stub {
            onBlocking { accessPhotos() } doReturn listOf(mockBitmap)
        }

        // Act
        val result = idVerification.accessPhotos(mockActivity)

        // Assert
        Assert.assertTrue(result is Either.Failure)
        Mockito.verify(mockAuthenticationService, Mockito.times(1)).authenticateUser(mockActivity)
        Mockito.verify(mockPhotoAccessService, Mockito.never()).accessPhotos()
    }

     @Test
    fun `takePhoto captures and stores photo successfully when permissions are granted`() = runTest {
        // Arrange
        mockCameraPermissionObserver.stub {
            onBlocking { checkPermission() } doReturn true
        }

        mockCameraService.stub {
            onBlocking { capturePhoto() } doReturn Either.Success(byteArrayOf(1, 2, 3))
        }

        mockPhotoAccessService.stub {
            onBlocking { saveImage(any(), any()) } doReturn true
        }

        // Act
        val result = idVerificationForTakePhoto.takePhoto()

        // Assert
        Assert.assertTrue(result is Either.Success<*>)
        Mockito.verify(mockCameraService, Mockito.times(1)).capturePhoto()
        Mockito.verify(mockPhotoAccessService, Mockito.times(1))
            .saveImage(any<String>(), any<ByteArray>())
    }

     @Test
    fun `takePhoto returns Failure when photo capture fails`() = runTest {
        // Arrange
        mockCameraPermissionObserver.stub {
            on { checkPermission() } doReturn true
        }
        mockCameraService.stub {
            onBlocking { capturePhoto() } doReturn Either.Failure(IDError.FailedInCapture)
        }

        // Act
        val result = idVerificationForTakePhoto.takePhoto()

        // Assert
        Assert.assertEquals(Either.Failure(IDError.FailedInCapture), result)
        Mockito.verify(mockCameraService, Mockito.times(1)).capturePhoto()
        Mockito.verify(mockPhotoAccessService, Mockito.never())
            .saveImage(any<String>(), any<ByteArray>())
    }

    @Test
    fun `takePhoto requests permissions and returns Failure when permissions are denied`() = runTest {
        // Arrange
        mockCameraPermissionObserver.stub {
            on { checkPermission() } doReturn false
        }

        mockCameraPermissionObserver.stub {
            on { launch() } doAnswer {
                idVerificationForTakePhoto.permissionDenied()
            }
        }

        var result: Either<Unit, IDError>? = null
        // Act
        result = idVerificationForTakePhoto.takePhoto()
        Mockito.verify(mockCameraService, Mockito.never()).capturePhoto()
        Mockito.verify(mockPhotoAccessService, Mockito.never()).saveImage(any<String>(), any<ByteArray>())
        // Assert
        Assert.assertEquals(Either.Failure(IDError.PermissionsNotProvided), result)
        Mockito.verify(mockCameraPermissionObserver, Mockito.times(1)).launch()
    }
}