package com.abbas.idlibrary.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

interface PermissionListener {
    fun permissionGranted()
    fun permissionDenied()
}

abstract class AbstractPermissionObserver : LifecycleEventObserver {

    var granted: Boolean = false
    var permissionListener: PermissionListener? = null

    abstract fun launch()

    abstract fun checkPermission(): Boolean

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> onCreate()
            Lifecycle.Event.ON_DESTROY -> onDestroy()
            else -> {}
        }
    }

    open fun onCreate() {}

    open fun onDestroy() {}
}

class CameraPermissionObserver(
    private val appCompatContext: Context
) : AbstractPermissionObserver() {

    private var requestPermission: ActivityResultLauncher<Array<String>>? = null

    override fun onCreate() {
        if (appCompatContext is ComponentActivity) {
            requestPermission = appCompatContext.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                permissions.entries.forEach {
                    granted = it.value
                    if (!it.value) {
                        return@forEach
                    }
                }
                if (granted) {
                    permissionListener?.permissionGranted()
                } else {
                    permissionListener?.permissionDenied()
                }
            }
        }
    }

    override fun onDestroy() {
        requestPermission = null
    }

    override fun launch() {
        requestPermission?.launch(arrayOf(
            Manifest.permission.CAMERA
        ))
    }

    override fun checkPermission(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(appCompatContext, Manifest.permission.CAMERA)
        return cameraPermission == PackageManager.PERMISSION_GRANTED
    }
}