package com.example.barterskill.adapters

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.barterskill.R
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

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userImageView: ImageView = itemView.findViewById(R.id.userImageView)
        val userNameTextView: TextView = itemView.findViewById(R.id.userNameTextView)
        val timeStampTextView: TextView = itemView.findViewById(R.id.timeStampTextView)
        val postTitleTextView: TextView = itemView.findViewById(R.id.postTitleTextView)
        val postDescriptionTextView: TextView = itemView.findViewById(R.id.postDescriptionTextView)
        val offerSkillTextView: TextView = itemView.findViewById(R.id.offerSkillTextView)
        val wantSkillTextView: TextView = itemView.findViewById(R.id.wantSkillTextView)
        val swapButton: Button = itemView.findViewById(R.id.swapButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        // Load user details
        firestore.collection("users").document(post.userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    holder.userNameTextView.text = document.getString("name")
                    // We would implement image loading here with a library like Glide
                    val base64Image = document.getString("base64ProfileImage")
                    if (!base64Image.isNullOrEmpty()) {
                        try {
                            val decodedBytes = android.util.Base64.decode(base64Image, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                            Glide.with(holder.itemView.context)
                                .load(bitmap)
                                .circleCrop()
                                .placeholder(R.drawable.ic_profile)
                                .into(holder.userImageView)

                        }catch (e: Exception) {
                            Glide.with(holder.itemView.context)
                                .load(R.drawable.ic_profile)
                                .circleCrop()
                                .into(holder.userImageView)
                        }
                    }
                }
            }

        // Format timestamp
        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
        holder.timeStampTextView.text = sdf.format(post.timestamp)

        // Set post details
        holder.postTitleTextView.text = post.title
        holder.postDescriptionTextView.text = post.description
        holder.offerSkillTextView.text = post.offerSkill
        holder.wantSkillTextView.text = post.wantSkill

        // Handle swap button
        if (post.userId == currentUserId) {
            holder.swapButton.visibility = View.GONE
        } else {
            holder.swapButton.visibility = View.VISIBLE
            holder.swapButton.setOnClickListener {
                onSwapClickListener(post)
            }
        }
    }

    override fun getItemCount(): Int = posts.size
}