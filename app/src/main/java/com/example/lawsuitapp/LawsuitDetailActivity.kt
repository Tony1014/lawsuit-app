package com.example.lawsuitapp
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class LawsuitDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lawsuit_detail)

        val title = intent.getStringExtra("title") ?: ""
        val difficulty = intent.getStringExtra("difficulty") ?: ""
        val compensationType = intent.getStringExtra("compensation_type") ?: "unknown"
        val claimRequired = intent.getBooleanExtra("claim_required", false)
        val proofRequired = intent.getBooleanExtra("proof_required", false)
        val sourceUrl = intent.getStringExtra("source_url") ?: ""

        findViewById<TextView>(R.id.titleText).text = title
        val claimText = if (claimRequired) "Claim required" else "No claim required"
        val proofText = if (proofRequired) "Proof required" else "No proof required"

        findViewById<TextView>(R.id.detailText).text =
            "Difficulty: $difficulty\nClaim: $claimText\nProof required: $proofText\nCompensation type: $compensationType\n\nSource:\n$sourceUrl"
        findViewById<Button>(R.id.openSourceBtn).setOnClickListener {
            if (sourceUrl.isNotBlank()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(sourceUrl))
                startActivity(intent)
            }
        }
    }
}