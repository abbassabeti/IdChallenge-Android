package com.abbas.idlibrary
import android.content.Context
import androidx.activity.result.ActivityResultRegistry
import androidx.lifecycle.LifecycleOwner

class VerificationDependencies(
    val context: Context,
    val lifecycleOwner: LifecycleOwner,
    val activityResultRegistry: ActivityResultRegistry
)

interface VerificationOwner {
    val dependencies: VerificationDependencies
}

