package com.example.lawsuitapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lawsuitapp.R
import com.example.lawsuitapp.data.remote.RetrofitInstance
import com.example.lawsuitapp.data.repository.LawsuitRepository
import com.example.lawsuitapp.ui.detail.LawsuitDetailActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var spClaimRequired: Spinner
    private lateinit var spProof: Spinner
    private lateinit var spDiff: Spinner
    private lateinit var btnApply: Button
    private lateinit var tvStatus: TextView
    private lateinit var rv: RecyclerView

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(
            LawsuitRepository(RetrofitInstance.api)
        )
    }

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

        /** val spClaimRequired: Spinner = findViewById(R.id.spClaimRequired)
        val spProof: Spinner = findViewById(R.id.spProofRequired)
        val spDiff: Spinner = findViewById(R.id.spDifficulty)
        val btnApply: Button = findViewById(R.id.btnApply)
        val tvStatus: TextView = findViewById(R.id.tvStatus)
        val rv: RecyclerView = findViewById(R.id.rvLawsuits)
        Move all the variables outside from local variables, so can use outside onCreate **/
        spClaimRequired = findViewById(R.id.spClaimRequired)
        spProof = findViewById(R.id.spProofRequired)
        spDiff = findViewById(R.id.spDifficulty)
        btnApply = findViewById(R.id.btnApply)
        tvStatus = findViewById(R.id.tvStatus)
        rv = findViewById(R.id.rvLawsuits)

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        // Spinner options
        val boolOptions = listOf("Any", "true", "false")
        val diffOptions = listOf("Any", "easy", "medium", "hard")

        spClaimRequired.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, boolOptions)
        spProof.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, boolOptions)
        spDiff.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, diffOptions)

        observeViewModel()
        btnApply.setOnClickListener {
            applyFilters()
        }
        // Auto-load once on start
        applyFilters()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    tvStatus.text = when {
                        state.isLoading -> "Loading..."
                        state.errorMessage != null -> "Error: ${state.errorMessage}"
                        else -> "Loaded ${state.lawsuits.size} lawsuits"
                    }
                    adapter.submitList(state.lawsuits)
                }
            }
        }
    }

    private fun applyFilters() {
        viewModel.fetchLawsuits(
            claimRequired = parseBoolSpinner(spClaimRequired),
            proofRequired = parseBoolSpinner(spProof),
            difficulty = parseDiffSpinner(spDiff)
        )
    }

    private fun parseBoolSpinner(sp: Spinner): Boolean? {
        return when (sp.selectedItem.toString()) {
            "true" -> true
            "false" -> false
            else -> null
        }
    }

    private fun parseDiffSpinner(sp: Spinner): String? {
        val value = sp.selectedItem.toString()
        return if (value == "Any") null else value
    }
}

/**
 Old code before MVVM:
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
 **/