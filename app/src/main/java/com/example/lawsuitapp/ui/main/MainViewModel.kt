package com.example.lawsuitapp.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lawsuitapp.data.repository.LawsuitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: LawsuitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState

    fun fetchLawsuits(
        claimRequired: Boolean? = null,
        proofRequired: Boolean? = null,
        difficulty: String? = null,
        compensationType: String? = null
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = MainUiState(isLoading = true)

                val lawsuits = repository.getLawsuits(
                    claimRequired = claimRequired,
                    proofRequired = proofRequired,
                    difficulty = difficulty,
                    compensationType = compensationType
                )

                _uiState.value = MainUiState(
                    isLoading = false,
                    lawsuits = lawsuits
                )

            } catch (e: Exception) {
                _uiState.value = MainUiState(
                    isLoading = false,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }
}