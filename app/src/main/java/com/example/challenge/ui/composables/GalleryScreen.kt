package com.example.challenge.ui.composables

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat.GroupAlertBehavior
import com.example.challenge.ui.theme.ChallengeTheme

@Composable
fun GalleryScreen(
    images: List<Bitmap>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 3,
    contentPadding: PaddingValues = PaddingValues(4.dp),
    itemSpacing: Int = 4
) {
    BackHandler(onBack = onBack)
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(itemSpacing.dp),
        horizontalArrangement = Arrangement.spacedBy(itemSpacing.dp)
    ) {
        items(images) { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null, // Provide a description if needed for accessibility
                contentScale = ContentScale.Crop, // Adjust as per your requirement
                modifier = Modifier
                    .aspectRatio(1f) // Makes each grid item square
                    .padding(2.dp) // Optional padding around each image
            )
        }
    }
}