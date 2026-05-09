package com.example.lawsuitapp.data.repository

import com.example.lawsuitapp.data.model.Lawsuit
import com.example.lawsuitapp.data.remote.APIService

class LawsuitRepository(
    private val apiService: APIService
) {
    suspend fun getLawsuits(
        claimRequired: Boolean?,
        proofRequired: Boolean?,
        difficulty: String?,
        compensationType: String?
    ): List<Lawsuit> {
        return apiService.getLawsuits(
            claimRequired = claimRequired,
            proofRequired = proofRequired,
            difficulty = difficulty,
            compensationType = null
        )
    }
}
