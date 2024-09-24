# **Android SDK - ID Verification**

This Android SDK provides a secure and modular solution for handling user identity verification, including capturing user photos, encrypting sensitive data, and managing biometric authentication. The SDK ensures that the sensitive data is stored securely and that access is controlled through authentication mechanisms.

## **Overview**

The ID Verification SDK is designed with a focus on modularity, security, and ease of use. It includes the following core features:

1. **Photo Capture**: Securely capture user photos using the device's camera.
2. **Encryption**: Encrypt captured photos before saving them to internal storage.
3. **Biometric Authentication**: Authenticate users using biometric methods (e.g., fingerprint, face recognition).
4. **Coroutines**: All asynchronous tasks such as encryption, decryption, and file operations are handled using Kotlin **coroutines** for clean and efficient code execution.
5. **Storage**: Manage encrypted images in the app's internal storage, ensuring that they remain inaccessible to other apps.

## **Key Features**

1. **Modular Architecture**:
    - Designed with a clear separation between responsibilities, making the SDK easily testable and extendable.
    - Dependency injection is used throughout the SDK using **Dagger Hilt** to keep components loosely coupled.

2. **Asynchronous Operations Using Coroutines**:
    - The SDK leverages Kotlin **coroutines** for handling time-consuming operations such as photo capturing, encryption, and decryption.
    - Long-running operations are implemented as `suspend` functions to provide a non-blocking user experience.

3. **Biometric Authentication**:
    - The SDK uses the **BiometricPrompt API** to authenticate users with fingerprints or face recognition.
    - Secure access to encrypted photos is granted only after successful biometric authentication.

4. **Encryption**:
    - Data is encrypted using **AES encryption** to ensure that photos are stored securely in the device's internal storage.

## **Challenges Faced**

### 1. **Converting Handlers and Closures to Coroutines**
One of the key challenges faced during the development of this SDK was converting the traditional Android asynchronous patterns (e.g., handlers, callbacks) into **coroutines**.

The original implementation used handlers and closures for managing callbacks. However, using coroutines provided a cleaner, more scalable, and modern approach to handling asynchronous tasks. This was particularly useful for:

- **Camera operations** (e.g., capturing photos)
- **File I/O operations** (e.g., saving and retrieving encrypted images)
- **Authentication callbacks** (e.g., biometric authentication success/failure)

Coroutines made the code more readable by eliminating callback hell and providing direct `suspend` functions for cleaner sequential code.

### 2. **Making Biometric Features of the OS Testable**
Another cool challenge was to make our implementation of Biometric Authentication to be testable. Initially, my AuthenticationService was tightly coupled with BiometricManager of the OS and that made the testing difficult. I finally separated the Biometric Authentication module into two separate modules to make it more testable and decoupled from our own logic.

### 2. **Using `suspend` Functions for Asynchronous Tasks**
To achieve non-blocking operations, all long-running tasks (like capturing photos, encryption, and biometric authentication) were converted to `suspend` functions using Kotlin coroutines. This allows:

- Clear handling of asynchronous code.
- The ability to easily perform operations on different dispatchers (e.g., `Dispatchers.IO` for file I/O).
- Simplified handling of completion callbacks.

**Example of Coroutines:**

   ```kotlin
   suspend fun encryptAndStorePhoto(photoData: ByteArray): Boolean = withContext(Dispatchers.IO) {
       try {
           val encryptedData = encryptPhoto(photoData)
           storageService.saveEncryptedImage("photo.jpg", encryptedData)
           true
       } catch (e: Exception) {
           false
       }
   }
   ```

## **Architecture Overview**

The SDK is built with a clean architecture in mind. Each component is decoupled, making the SDK easier to test, maintain, and extend.

### **Key Components:**

1. **PhotoManager**:
    - Handles all interactions with the device's camera.
    - Captures photos, converts them into `ByteArray`, and passes the data to the encryption module.
    - Asynchronous operations are managed using coroutines.

2. **CryptoManager**:
    - Responsible for encrypting and decrypting the photo data using AES encryption.
    - Uses `suspend` functions to encrypt and decrypt without dealing with closures and handlers.

3. **BiometricAuthenticationManager**:
    - Handles biometric authentication using the **BiometricPrompt API**.
    - Ensures that access to sensitive data (photos) is granted only to authenticated users.

4. **LocalStorageManager**:
    - Manages saving, retrieving, and deleting encrypted images from the device's internal storage.
    - Uses secure internal storage to keep sensitive data isolated from other apps.

### **Core Libraries Used**:
- **Kotlin Coroutines**: For managing asynchronous tasks cleanly and efficiently.
- **Dagger Hilt**: For dependency injection, keeping the SDK modular and easily testable.
- **BiometricPrompt API**: For secure biometric authentication.
- **AES Encryption**: For encrypting and decrypting sensitive data.

## **Usage**

### 1. **Add the SDK to Your Project**

You can add the `.aar` file (Android Archive) to your project by importing it as a module. Make sure to set up the dependencies properly.

### 2. **Initialize the SDK in Your App**

You can initialize the SDK and use its components like this:

#### **Photo Capture Example:**

```kotlin
val idVerification = Id.initialize(this)
val result = idVerification.takePhoto()
when (result) {
    is Success -> {
        // Image taken and stored
    }
    is Failure -> {
        // Handle Failure based on mapping IDError
    }
}
```

#### **Biometric Authentication Example:**

```kotlin
val idVerification = Id.initialize(this)
val result = idVerification.authenticateUser(activity: this)
when (result) {
    is Success -> {
        // Provide access
    }
    is Failure -> {
        // Handle Failure based on mapping IDError
    }
}
```

### 3. **Handling Permissions**
The SDK requires the **Camera** and **Biometric** permissions. Make sure to include the following in your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.USE_BIOMETRIC" />
```

Additionally, ensure you request permissions at runtime using the **ActivityResultLauncher** or **Permission APIs**.

### 4. **Handling Encrypted Data**
All data captured by the camera is encrypted before it is saved. To retrieve encrypted photos, use the following method:

```kotlin
val idVerification = Id.initialize(this)
val result = idVerification.accessPhotos(activity: this)
when (result) {
    is Success -> {
        // Present images
    }
    is Failure -> {
        // Handle Failure based on mapping IDError
    }
}
```

## **Testing**

Unit tests were written for each module using **JUnit** and **Mockito**. Special attention was given to testing the asynchronous nature of the SDK by using **TestCoroutineDispatcher** for coroutine testing.

Here’s how you can run the tests:

1. Open the Android project in Android Studio.
2. Right-click the `test` folder in your project and select **Run All Tests**.
3. The results will show up in the Android Studio test runner.

It might be useful to mention that for handling takePhoto tests, I had to use real Dispatchers for Coroutines instead of the TestDispatchers. Looks like when I use TestDispatchers, the methods are not getting executed in the coroutines of the main module of sdk. Since this is only in the scope of test section of the library, I believe as long as tests are being executed correctly and they are green, it doesn't harm to do this. So, that's the reason that I initialized two instances of IdVerification in the IdVerificationTests.kt.

## **Release process**

For releasing the xcframework, I created a script (build_aar.sh) in the root folder. This creates the proper aar build. For checking that everything works fine, I tried importing this in another iOS project and it works fine. For reference you can find the aar output [here](https://github.com/abbassabeti/IdChallenge-Android/blob/build_manual_upload/Releases/idlibrary-release.aar) in the repository.

## **Further Development**

Currently, The SDK supports all the devices which has Biometric Authentication features. But in order to have more compatibility, I believe we need to add support for devices which don't have Biometric Features. At least by having a PIN or pattern to authenticate if the user is Authorized to access photos. Considering the amount of time needed for addressing this, I believe it makes sense to consider this as a furhter step in development.

## **Time dedicated to implement this SDK**

I spend around half a day for the Android SDK and the main app to make it work. Then I spend another half a day later this week to add tests and make things more testable in my SDK. So, in total, it took around a day to create this and then for adding this document, I spent like 1 more hour.
