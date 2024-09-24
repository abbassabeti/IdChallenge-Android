package com.example.challenge.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.challenge.ui.theme.ChallengeTheme

@Composable
fun MainScreen(buttonClick: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp, alignment = Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { buttonClick(1) },
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(text = "Take Photo")
        }

        Button(
            onClick = { buttonClick(2) },
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(text = "Authenticate User")
        }

        Button(
            onClick = { buttonClick(3) },
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(text = "Access Photos")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ChallengeTheme {
        MainScreen({ _ -> })
    }
}