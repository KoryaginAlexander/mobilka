package com.example.pr_6_2.presentation.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.pr_6_2.domain.model.NobelLaureate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NobelDetailScreen(
    laureate: NobelLaureate,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laureate Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (laureate.portraitUrl != null) {
                SubcomposeAsyncImage(
                    model = laureate.portraitUrl,
                    contentDescription = laureate.fullName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(160.dp)
                        .clip(MaterialTheme.shapes.large),
                    loading = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        }
                    },
                    error = { NoPortraitPlaceholder(name = laureate.fullName) }
                )
            } else {
                NoPortraitPlaceholder(name = laureate.fullName)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = laureate.fullName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text(laureate.year) })
                AssistChip(onClick = {}, label = { Text(laureate.category) })
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            if (laureate.motivation.isNotBlank()) {
                DetailSection(title = "Motivation", body = laureate.motivation)
                Spacer(modifier = Modifier.height(16.dp))
            }

            val location = buildString {
                if (laureate.birthCity.isNotBlank()) append(laureate.birthCity)
                if (laureate.birthCity.isNotBlank() && laureate.birthCountry.isNotBlank()) append(", ")
                if (laureate.birthCountry.isNotBlank()) append(laureate.birthCountry)
            }
            if (location.isNotBlank()) {
                DetailSection(title = "Born in", body = location)
            }
        }
    }
}

@Composable
private fun DetailSection(title: String, body: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun NoPortraitPlaceholder(name: String) {
    Surface(
        modifier = Modifier.size(160.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = name.firstOrNull()?.toString() ?: "?",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
