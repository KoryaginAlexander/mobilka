package com.example.pr_6_2.presentation.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.pr_6_2.domain.model.NobelLaureate

val CATEGORIES = listOf(
    null to "All categories",
    "physics" to "Physics",
    "chemistry" to "Chemistry",
    "literature" to "Literature",
    "peace" to "Peace",
    "medicine" to "Medicine",
    "economic sciences" to "Economics"
)

val YEARS = listOf(null) + (2024 downTo 1901).toList()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NobelListScreen(
    viewModel: NobelListViewModel,
    onLaureateClick: (NobelLaureate) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nobel Prize Laureates") },
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
        ) {
            FilterRow(
                selectedYear = selectedYear,
                selectedCategory = selectedCategory,
                onYearSelected = viewModel::setYear,
                onCategorySelected = viewModel::setCategory
            )

            when (val state = uiState) {
                is NobelListUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is NobelListUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Error: ${state.message}",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                            Button(onClick = viewModel::loadLaureates) {
                                Text("Retry")
                            }
                        }
                    }
                }
                is NobelListUiState.Success -> {
                    if (state.laureates.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No laureates found")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.laureates) { laureate ->
                                LaureateCard(laureate = laureate, onClick = { onLaureateClick(laureate) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterRow(
    selectedYear: Int?,
    selectedCategory: String?,
    onYearSelected: (Int?) -> Unit,
    onCategorySelected: (String?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DropdownSelector(
            modifier = Modifier.weight(1f),
            label = "Year",
            options = YEARS.map { it?.toString() ?: "All years" },
            selectedIndex = YEARS.indexOf(selectedYear).coerceAtLeast(0),
            onSelected = { index -> onYearSelected(YEARS[index]) }
        )
        DropdownSelector(
            modifier = Modifier.weight(1f),
            label = "Category",
            options = CATEGORIES.map { it.second },
            selectedIndex = CATEGORIES.indexOfFirst { it.first == selectedCategory }.coerceAtLeast(0),
            onSelected = { index -> onCategorySelected(CATEGORIES[index].first) }
        )
    }
}

@Composable
private fun DropdownSelector(
    modifier: Modifier = Modifier,
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = options.getOrElse(selectedIndex) { label },
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(index)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun LaureateCard(laureate: NobelLaureate, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = laureate.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = laureate.year,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            AssistChip(
                onClick = {},
                label = { Text(laureate.category, style = MaterialTheme.typography.labelSmall) }
            )
            if (laureate.motivation.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = laureate.motivation.take(100).let {
                        if (laureate.motivation.length > 100) "$it…" else it
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
