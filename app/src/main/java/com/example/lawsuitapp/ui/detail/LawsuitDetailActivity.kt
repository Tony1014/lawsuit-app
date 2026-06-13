package com.example.lawsuitapp.ui.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class LawsuitDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val title = intent.getStringExtra("title") ?: ""
        val difficulty = intent.getStringExtra("difficulty") ?: ""
        val compensationType = intent.getStringExtra("compensation_type") ?: "unknown"
        val claimRequired = intent.getBooleanExtra("claim_required", false)
        val proofRequired = intent.getBooleanExtra("proof_required", false)
        val caseStatus = intent.getStringExtra("case_status") ?: "Unknown"
        val sourceUrl = intent.getStringExtra("source_url") ?: ""

        setContent {
            MaterialTheme {
                LawsuitDetailScreen(
                    title = title,
                    difficulty = difficulty,
                    compensationType = compensationType,
                    claimRequired = claimRequired,
                    proofRequired = proofRequired,
                    caseStatus = caseStatus,
                    sourceUrl = sourceUrl,
                    onOpenSource = {
                        openSourcePage(sourceUrl)
                    }
                )
            }
        }
    }

    private fun openSourcePage(sourceUrl: String) {
        if (sourceUrl.isBlank()) return

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(sourceUrl))
        startActivity(intent)
    }
}

@Composable
fun LawsuitDetailScreen(
    title: String,
    difficulty: String,
    compensationType: String,
    claimRequired: Boolean,
    proofRequired: Boolean,
    caseStatus: String,
    sourceUrl: String,
    onOpenSource: () -> Unit
) {
    val claimText = if (claimRequired) "Yes" else "No"
    val proofText = if (proofRequired) "Yes" else "No"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "App",
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = "Lawsuit Detail",
            style = TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight(500),
                color = Color(0xFF000000)
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontSize = 18.sp,
                        lineHeight = 26.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2B2B2B)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                DetailRow(label = "Status", value = caseStatus)
                DetailRow(label = "Difficulty", value = difficulty)
                DetailRow(label = "Compensation", value = compensationType)
                DetailRow(label = "Claim Required", value = claimText)
                DetailRow(label = "Proof Required", value = proofText)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = sourceUrl.isNotBlank()) {
                    onOpenSource()
                }
        ) {
            Text(
                text = if (sourceUrl.isNotBlank()) "Open source website" else "No source website available",
                modifier = Modifier.padding(16.dp),
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

@Composable
fun DetailRow(label: String, value: String) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "$label:",
            modifier = Modifier.weight(1f),
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2B2B2B)
            )
        )

        Text(
            text = value.replaceFirstChar { it.uppercase() },
            modifier = Modifier.weight(1f),
            style = TextStyle(
                fontSize = 14.sp,
                color = Color(0xFF666666)
            )
        )
    }
}

@Composable
fun DetailText(text: String) {
    Text(
        text = text,
        style = TextStyle(
            fontSize = 14.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF666666)
        )
    )
}

@Preview(showBackground = true)
@Composable
fun LawsuitDetailScreenPreview() {
    MaterialTheme {
        LawsuitDetailScreen(
            title = "Apple Class Action Settlement",
            difficulty = "easy",
            compensationType = "money",
            claimRequired = true,
            proofRequired = false,
            caseStatus = "In Progress",
            sourceUrl = "https://example.com",
            onOpenSource = {}
        )
    }
}