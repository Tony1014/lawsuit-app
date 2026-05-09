package com.example.lawsuitapp.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lawsuitapp.R
import com.example.lawsuitapp.data.model.Lawsuit

class LawsuitAdapter(private val onTitleClick: (Lawsuit) -> Unit) : RecyclerView.Adapter<LawsuitAdapter.VH>() {

    private var items: List<Lawsuit> = emptyList()

    fun submitList(newItems: List<Lawsuit>) {
        items = newItems
        notifyDataSetChanged()
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvMeta: TextView = itemView.findViewById(R.id.tvMeta)
        val tvCaseStatus: TextView = itemView.findViewById(R.id.tvCaseStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_lawsuit, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val l = items[position]
        holder.tvTitle.text = l.title
        holder.tvMeta.text = "claim_required=${l.claim_required} • proof_required=${l.proof_required} • compensation_type=${l.compensation_type} • difficulty=${l.difficulty}"
        holder.tvCaseStatus.text = l.case_status
        // Click title -> open detail
        holder.tvTitle.setOnClickListener {
            onTitleClick(l)
        }
    }
}