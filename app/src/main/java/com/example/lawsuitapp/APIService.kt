package com.example.lawsuitapp

import retrofit2.http.GET
import retrofit2.http.Query

interface APIService {
    @GET("lawsuits")
    suspend fun getLawsuits(
        @Query("claim_required") claimRequired: Boolean?,
        @Query("proof_required") proofRequired: Boolean?,
        @Query("difficulty") difficulty: String?,
        @Query("compensation_type") compensationType: String?
    ): List<Lawsuit>
}