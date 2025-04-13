package com.example.barterskill.fragments.main

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.barterskill.R
import com.example.barterskill.adapters.PostAdapter
import com.example.barterskill.adapters.SkillAdapter
import com.example.barterskill.databinding.FragmentExplorerBinding
import com.example.barterskill.models.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

class ExplorerFragment : Fragment() {

    private var _binding: FragmentExplorerBinding? = null
    private val binding get() = _binding!!

    private val postsList = mutableListOf<Post>()
    private lateinit var postAdapter: PostAdapter

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExplorerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView
        binding.recyclerViewPosts.layoutManager = LinearLayoutManager(context)
        postAdapter = PostAdapter(postsList) { post ->
            sendSwapRequest(post)
        }
        binding.recyclerViewPosts.adapter = postAdapter

        // Setup search functionality
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    searchPosts(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    loadPosts()
                }
                return true
            }
        })


        // FAB to create a new post
        binding.fabCreatePost.setOnClickListener {
            findNavController().navigate(R.id.createPostFragment)
        }

        loadUserProfile()
        loadPosts()
    }

    override fun onResume() {
        super.onResume()
        loadPosts()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadPosts() {
        firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                postsList.clear()
                for (document in documents) {
                    val post = document.toObject(Post::class.java).copy(id = document.id)
                    postsList.add(post)
                }
                postAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error loading posts: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun searchPosts(query: String) {
        firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                postsList.clear()
                for (document in documents) {
                    val post = document.toObject(Post::class.java).copy(id = document.id)
                    if (post.title.contains(query, ignoreCase = true) ||
                        post.description.contains(query, ignoreCase = true) ||
                        post.offerSkill.contains(query, ignoreCase = true) ||
                        post.wantSkill.contains(query, ignoreCase = true)) {
                        postsList.add(post)
                    }
                }
                postAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error searching posts: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendSwapRequest(post: Post) {
        val currentUser = auth.currentUser ?: return

        if (post.userId == currentUser.uid) {
            Toast.makeText(context, "You cannot swap with your own post", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val currentUserName = document.getString("name") ?: "A user"
                    val notification = hashMapOf(
                        "type" to "swap_request",
                        "senderId" to currentUser.uid,
                        "recipientId" to post.userId,
                        "postId" to post.id,
                        "details" to mapOf(
                            "senderSkill" to post.wantSkill,
                            "recipientSkill" to post.offerSkill,
                            "postTitle" to post.title
                        ),
                        "timestamp" to Date(),
                        "read" to false
                    )

                    firestore.collection("notifications")
                        .add(notification)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Swap request sent!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error sending swap request: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
    }

    private fun loadUserProfile() {
        val userId = firebaseAuth.currentUser?.uid ?: return

        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val base64Image = document.getString("base64ProfileImage")
                    if (!base64Image.isNullOrEmpty()) {
                        try {
                            val decodedBytes = android.util.Base64.decode(base64Image, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                            Glide.with(this)
                                .load(bitmap)
                                .circleCrop()
                                .placeholder(R.drawable.ic_profile)
                                .into(binding.profileImageView)
                        } catch (e: Exception) {
                            Glide.with(this)
                                .load(R.drawable.ic_profile)
                                .circleCrop()
                                .into(binding.profileImageView)
                        }
                    } else {
                        Glide.with(this)
                            .load(R.drawable.ic_profile)
                            .circleCrop()
                            .into(binding.profileImageView)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load user profile", Toast.LENGTH_SHORT).show()
            }
    }
}