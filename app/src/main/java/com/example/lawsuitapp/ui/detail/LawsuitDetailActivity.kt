package com.example.lawsuitapp.ui.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.lawsuitapp.R

class LawsuitDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lawsuit_detail)

        val title = intent.getStringExtra("title") ?: ""
        val difficulty = intent.getStringExtra("difficulty") ?: ""
        val compensationType = intent.getStringExtra("compensation_type") ?: "unknown"
        val claimRequired = intent.getBooleanExtra("claim_required", false)
        val proofRequired = intent.getBooleanExtra("proof_required", false)
        val caseStatus = intent.getStringExtra("case_status") ?: "Unknown"
        findViewById<TextView>(R.id.tvDetailCaseStatus).text = caseStatus
        val sourceUrl = intent.getStringExtra("source_url") ?: ""

        findViewById<TextView>(R.id.titleText).text = title
        val claimText = if (claimRequired) "Yes" else "No"
        val proofText = if (proofRequired) "Yes" else "No"

        val openSourceBtn = findViewById<Button>(R.id.openSourceBtn)
        openSourceBtn.isEnabled = sourceUrl.isNotBlank()
        openSourceBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(sourceUrl))
            startActivity(intent)
        }
        findViewById<TextView>(R.id.detailText).text = """
            Difficulty: $difficulty
            Compensation: $compensationType
            Claim Required: $claimText
            Proof Required: $proofText
        """.trimIndent()
    }
}