package com.example.lawsuitapp.ui.main

import com.example.lawsuitapp.data.model.Lawsuit

data class MainUiState(
    val isLoading: Boolean = false,
    val lawsuits: List<Lawsuit> = emptyList(),
    val errorMessage: String? = null
)
