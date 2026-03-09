package com.example.lawsuitapp
import com.google.gson.annotations.SerializedName
data class Lawsuit(
    val id: Int,
    val title: String,
    val claim_required: Boolean,
    val proof_required: Boolean,
    val difficulty: String,
    val compensation_type: String,
    val source_url: String
)
