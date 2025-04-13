package com.example.barterskill.adapters

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.barterskill.R
import com.example.barterskill.databinding.ItemPostBinding
import com.example.barterskill.models.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class PostAdapter(
    private val posts: List<Post>,
    private val onSwapClickListener: (Post) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private val firestore = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    class PostViewHolder(
        val binding: ItemPostBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        // Load user details
        firestore.collection("users").document(post.userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    holder.binding.userNameTextView.text = document.getString("name")
                    val base64Image = document.getString("base64ProfileImage")
                    if (!base64Image.isNullOrEmpty()) {
                        try {
                            val decodedBytes = Base64.decode(base64Image, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                            Glide.with(holder.itemView.context)
                                .load(bitmap)
                                .circleCrop()
                                .placeholder(R.drawable.ic_profile)
                                .into(holder.binding.userImageView)
                        } catch (e: Exception) {
                            Glide.with(holder.itemView.context)
                                .load(R.drawable.ic_profile)
                                .circleCrop()
                                .into(holder.binding.userImageView)
                        }
                    }
                }
            }

        // Format timestamp
        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
        holder.binding.timeStampTextView.text = sdf.format(post.timestamp)

        // Set post details
        holder.binding.postTitleTextView.text = post.title
        holder.binding.postDescriptionTextView.text = post.description
        holder.binding.offerSkillTextView.text = post.offerSkill
        holder.binding.wantSkillTextView.text = post.wantSkill

        // Handle swap button
        if (post.userId == currentUserId) {
            holder.binding.swapButton.visibility = android.view.View.GONE
        } else {
            holder.binding.swapButton.visibility = android.view.View.VISIBLE
            holder.binding.swapButton.setOnClickListener {
                onSwapClickListener(post)
            }
        }
    }

    override fun getItemCount(): Int = posts.size
}