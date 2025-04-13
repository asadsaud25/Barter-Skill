package com.example.barterskill.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.barterskill.databinding.ItemSkillBinding

class SkillAdapter(private val skills: List<String>) :
    RecyclerView.Adapter<SkillAdapter.SkillViewHolder>() {

    class SkillViewHolder(
        val binding: ItemSkillBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillViewHolder {
        val binding = ItemSkillBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SkillViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SkillViewHolder, position: Int) {
        holder.binding.skillTextView.text = skills[position]
    }

    override fun getItemCount(): Int = skills.size
}