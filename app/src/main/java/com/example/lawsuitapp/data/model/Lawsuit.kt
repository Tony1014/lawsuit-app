package com.example.lawsuitapp.data.model

data class Lawsuit(
    val id: Int,
    val title: String,
    val claim_required: Boolean,
    val proof_required: Boolean,
    val difficulty: String,
    val compensation_type: String,
    val case_status: String,
    val source_url: String
)