package com.example.lawsuitapp

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val adapter by lazy {
        LawsuitAdapter { l ->
            val intent = Intent(this, LawsuitDetailActivity::class.java).apply {
                putExtra("id", l.id)
                putExtra("title", l.title)
                putExtra("claim_required", l.claim_required)
                putExtra("compensation_type", l.compensation_type)
                putExtra("proof_required", l.proof_required)
                putExtra("difficulty", l.difficulty)
                putExtra("case_status", l.case_status)
                putExtra("source_url", l.source_url)
            }
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val spClaimRequired: Spinner = findViewById(R.id.spClaimRequired)
        val spProof: Spinner = findViewById(R.id.spProofRequired)
        val spDiff: Spinner = findViewById(R.id.spDifficulty)
        val btnApply: Button = findViewById(R.id.btnApply)
        val tvStatus: TextView = findViewById(R.id.tvStatus)
        val rv: RecyclerView = findViewById(R.id.rvLawsuits)

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        // Spinner options
        val boolOptions = listOf("Any", "true", "false")
        val diffOptions = listOf("Any", "easy", "medium", "hard")

        spClaimRequired.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, boolOptions)
        spProof.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, boolOptions)
        spDiff.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, diffOptions)

        fun parseBoolSpinner(sp: Spinner): Boolean? {
            return when (sp.selectedItem.toString()) {
                "true" -> true
                "false" -> false
                else -> null
            }
        }

        fun parseDiffSpinner(sp: Spinner): String? {
            val v = sp.selectedItem.toString()
            return if (v == "Any") null else v
        }

        suspend fun load() {
            tvStatus.text = "Loading..."
            try {
                val claimRequired = parseBoolSpinner(spClaimRequired)
                val proofRequired = parseBoolSpinner(spProof)
                val difficulty = parseDiffSpinner(spDiff)

                // IMPORTANT: use positional args (matches APIService order) to avoid name mismatch issues
                val lawsuits = apiService.getLawsuits(claimRequired, proofRequired, difficulty, null)

                adapter.submitList(lawsuits)
                tvStatus.text = "Loaded ${lawsuits.size} lawsuits"
            } catch (e: Exception) {
                tvStatus.text = "Error: ${e.message}"
            }
        }

        btnApply.setOnClickListener {
            lifecycleScope.launch { load() }
        }

        // Auto-load once on start
        lifecycleScope.launch { load() }
    }
}