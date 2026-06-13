package com.example.lawsuitapp.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lawsuitapp.data.model.Lawsuit
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.lawsuitapp.data.remote.RetrofitInstance
import com.example.lawsuitapp.data.repository.LawsuitRepository
import com.example.lawsuitapp.ui.detail.LawsuitDetailActivity


class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(
            LawsuitRepository(RetrofitInstance.api)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                MainScreen(
                    viewModel = viewModel,
                    onLawsuitClick = { lawsuit ->
                        openDetailPage(lawsuit)
                    }
                )
            }
        }
    }

    private fun openDetailPage(lawsuit: Lawsuit) {
        val intent = Intent(this, LawsuitDetailActivity::class.java).apply {
            putExtra("id", lawsuit.id)
            putExtra("title", lawsuit.title)
            putExtra("claim_required", lawsuit.claim_required)
            putExtra("compensation_type", lawsuit.compensation_type)
            putExtra("proof_required", lawsuit.proof_required)
            putExtra("difficulty", lawsuit.difficulty)
            putExtra("case_status", lawsuit.case_status)
            putExtra("source_url", lawsuit.source_url)
        }
        startActivity(intent)
    }
}

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onLawsuitClick: (Lawsuit) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    var selectedClaimRequired by remember { mutableStateOf("Any") }
    var selectedProofRequired by remember { mutableStateOf("Any") }
    var selectedDifficulty by remember { mutableStateOf("Any") }

    LaunchedEffect(
        selectedClaimRequired,
        selectedProofRequired,
        selectedDifficulty
    ) {
        viewModel.fetchLawsuits(
            claimRequired = parseBoolValue(selectedClaimRequired),
            proofRequired = parseBoolValue(selectedProofRequired),
            difficulty = parseStringValue(selectedDifficulty)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "App",
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight(400),
                color = Color(0xFF000000),
            )
        )

        Text(
            text = "Lawsuit",
            style = TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight(500),
                color = Color(0xFF000000),
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        FilterRow(
            label = "Claim require",
            options = listOf("Any", "true", "false"),
            selectedOption = selectedClaimRequired,
            onOptionSelected = { selectedClaimRequired = it }
        )

        Spacer(modifier = Modifier.height(10.dp))

        FilterRow(
            label = "Proof require",
            options = listOf("Any", "true", "false"),
            selectedOption = selectedProofRequired,
            onOptionSelected = { selectedProofRequired = it }
        )

        Spacer(modifier = Modifier.height(10.dp))

        FilterRow(
            label = "Difficulty",
            options = listOf("Any", "easy", "medium", "hard"),
            selectedOption = selectedDifficulty,
            onOptionSelected = { selectedDifficulty = it }
        )

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = when {
                state.isLoading -> "Loading..."
                state.errorMessage != null -> "Error: ${state.errorMessage}"
                else -> "Loaded ${state.lawsuits.size} lawsuits"
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(state.lawsuits) { lawsuit ->
                LawsuitRow(
                    lawsuit = lawsuit,
                    onClick = { onLawsuitClick(lawsuit) }
                )
            }
        }
    }
}

@Composable
fun FilterRow(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = TextStyle(
                fontSize = 13.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )
        )

        FilterDropdown(
            label = label,
            options = options,
            selectedOption = selectedOption,
            onOptionSelected = onOptionSelected,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun FilterDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(selectedOption)
                Text("⌄")
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun LawsuitRow(
    lawsuit: Lawsuit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = lawsuit.title,
                style = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2B2B2B),
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Status: ${lawsuit.case_status} • Difficulty: ${lawsuit.difficulty} • Compensation: ${lawsuit.compensation_type}",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF666666)
                )
            )
        }
    }
}

private fun parseBoolValue(value: String): Boolean? {
    return when (value) {
        "true" -> true
        "false" -> false
        else -> null
    }
}
private fun parseStringValue(value: String): String? {
    return if (value == "Any") null else value
}
