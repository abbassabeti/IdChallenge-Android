package com.example.challenge

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContent
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