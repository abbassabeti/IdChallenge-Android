package com.abbas.idlibrary

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.fragment.app.FragmentActivity
import com.abbas.idlibrary.usecases.AuthenticationServiceImpl
import com.abbas.idlibrary.utils.BiometricInterface
import com.abbas.idlibrary.utils.BiometricPromptBuilder
import com.abbas.idlibrary.utils.Either
import com.abbas.idlibrary.utils.IDError
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.whenever

class AuthenticationServiceImplTest {

    private lateinit var authenticationService: AuthenticationServiceImpl
    private lateinit var mockBiometricPromptBuilder: BiometricPromptBuilder
    private lateinit var mockBiometricPrompt: BiometricPrompt
    private lateinit var mockContext: FragmentActivity
    private lateinit var mockBiometricWrapper: BiometricInterface
    private lateinit var promptInfo: PromptInfo

    @Before
    fun setUp() {
        // Initialize mocks
        mockBiometricPromptBuilder = mock()
        mockBiometricPrompt = mock()
        mockContext = mock()
        mockBiometricWrapper = mock()
        promptInfo = mock()
        // Initialize AuthenticationServiceImpl with mocked BiometricPromptBuilder
        authenticationService = AuthenticationServiceImpl(
            mockBiometricPromptBuilder,
            mockBiometricWrapper
        )
//        mockBiometricPromptBuilder.stub {
//            on { buildBiometricPrompt(any(), any(), any()) } doReturn(mockBiometricPrompt)
//        }

        mockBiometricWrapper.stub {
            on { buildPromptInfo(any(), any(), any()) } doReturn promptInfo
        }
    }

    @After
    fun tearDown() {
//        clearAllMocks()
    }

    @Test
    fun `authenticateUser returns Success when biometric is available and authentication succeeds`() = runTest {
        // Arrange
        mockBiometricWrapper.stub {
            onBlocking { canAuthenticate(any()) } doReturn BiometricManager.BIOMETRIC_SUCCESS
        }

        mockBiometricPromptBuilder.stub {
            onBlocking { showPrompt(any(), any()) } doReturn Either.Success(Unit)
        }

        // Act
        val result = authenticationService.authenticateUser(mockContext)

        // Assert
        assertTrue(result is Either.Success)
    }

    @Test
    fun `authenticateUser returns Failure when biometric authentication fails`() = runTest {
        // Arrange
        mockBiometricWrapper.stub {
            onBlocking { canAuthenticate(any()) } doReturn BiometricManager.BIOMETRIC_SUCCESS
        }

        mockBiometricPromptBuilder.stub {
            onBlocking { showPrompt(any(), any()) } doReturn Either.Failure(IDError.AuthenticationFailed)
        }

        // Act
        val result = authenticationService.authenticateUser(mockContext)

        // Assert
        assertTrue(result is Either.Failure)
        assertEquals(IDError.AuthenticationFailed, (result as Either.Failure).value)
    }

    @Test
    fun `authenticateUser returns Failure when no biometric hardware exists`() = runTest {
        // Arrange
        whenever(mockBiometricWrapper.canAuthenticate(Authenticators.BIOMETRIC_STRONG))
            .thenReturn(BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE)

        // Act
        val result = authenticationService.authenticateUser(mockContext)

        // Assert
        assertTrue(result is Either.Failure)
        assertEquals(IDError.NoBiometryExists, (result as Either.Failure).value)
    }

    @Test
    fun `authenticateUser returns Failure when biometric hardware is unavailable`() = runTest {
        // Arrange
        whenever(mockBiometricWrapper.canAuthenticate(Authenticators.BIOMETRIC_STRONG))
            .thenReturn(BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE)

        // Act
        val result = authenticationService.authenticateUser(mockContext)

        // Assert
        assertTrue(result is Either.Failure)
        assertEquals(IDError.BiometryIsNotActivated, (result as Either.Failure).value)
    }

    @Test
    fun `authenticateUser returns Failure when no biometrics are enrolled`() = runTest {
        // Arrange
        whenever(mockBiometricWrapper.canAuthenticate(Authenticators.BIOMETRIC_STRONG))
            .thenReturn(BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED)

        // Act
        val result = authenticationService.authenticateUser(mockContext)

        // Assert
        assertTrue(result is Either.Failure)
        assertEquals(IDError.NoBiometryEnrolled, (result as Either.Failure).value)
    }

    @Test
    fun `authenticateUser returns Failure for unknown biometric error`() = runTest {
        // Arrange
        whenever(mockBiometricWrapper.canAuthenticate(Authenticators.BIOMETRIC_STRONG))
            .thenReturn(BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED)

        // Act
        val result = authenticationService.authenticateUser(mockContext)

        // Assert
        assertTrue(result is Either.Failure)
        assertEquals(IDError.BiometryIsNotAvailable, (result as Either.Failure).value)
    }
}