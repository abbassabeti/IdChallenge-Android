package com.example.challenge

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.abbas.idlibrary.Id
import com.abbas.idlibrary.IdVerification
import com.abbas.idlibrary.VerificationDependencies
import com.abbas.idlibrary.VerificationOwner
import com.abbas.idlibrary.utils.PermissionListener
import com.example.challenge.ui.UIState
import com.example.challenge.ui.composables.GalleryScreen
import com.example.challenge.ui.composables.MainScreen
import com.example.challenge.ui.theme.ChallengeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : VerificationOwner, AppCompatActivity(), PermissionListener {

    lateinit var viewModel: PrimaryViewModel

    lateinit var idVerification: IdVerification

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        idVerification = Id.initialize(this)
        enableEdgeToEdge()
        setContent {
            viewModel = hiltViewModel()
            viewModel.idVerification = idVerification
            ChallengeTheme {
                AnimatedContent(
                    targetState = viewModel.uiState.value, label = "transition",
                    ) {
                    when (val state = it) {
                        is UIState.Gallery -> {
                            GalleryScreen(images = state.images, onBack = viewModel::onBack)
                        }
                        UIState.Main -> {
                            MainScreen(buttonClick = { index ->
                                when (index) {
                                    1 -> {
                                        viewModel.takePhoto()
                                    }

                                    2 -> {
                                        viewModel.authenticateUser(this@MainActivity)
                                    }

                                    3 -> {
                                        viewModel.accessPhotos(this@MainActivity)
                                    }
                                }
                            })
                        }
                    }
                }
            }
        }
    }

    override fun permissionGranted() {
    }

    override fun permissionDenied() {
        Toast.makeText(this, "Required permissions denied!", Toast.LENGTH_LONG).show()
    }

    override val dependencies: VerificationDependencies
        get() = VerificationDependencies(
            lifecycleOwner = this,
            activityResultRegistry = activityResultRegistry,
            context = this
        )
}