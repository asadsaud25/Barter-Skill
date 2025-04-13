package com.example.barterskill.fragments.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.barterskill.R
import com.example.barterskill.adapters.PostAdapter
import com.example.barterskill.models.Post
import com.example.barterskill.models.Notification
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

class ExplorerFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var postAdapter: PostAdapter
    private val postsList = mutableListOf<Post>()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explorer, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewPosts)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize the adapter
        postAdapter = PostAdapter(postsList) { post ->
            sendSwapRequest(post)
        }
        recyclerView.adapter = postAdapter

        // Setup search functionality
        searchView = view.findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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
        val fabCreatePost = view.findViewById<FloatingActionButton>(R.id.fabCreatePost)
        fabCreatePost.setOnClickListener {
            findNavController().navigate(R.id.createPostFragment)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadPosts()
    }

    override fun onResume() {
        super.onResume()
        // Reload posts when returning to this fragment
        loadPosts()
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
        // This is a simple search - in a real app, you might want to use Firestore queries
        // or a more sophisticated search method
        firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                postsList.clear()
                for (document in documents) {
                    val post = document.toObject(Post::class.java).copy(id = document.id)

                    // Check if query matches any of the post fields
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

        // Check if the user is trying to swap with their own post
        if (post.userId == currentUser.uid) {
            Toast.makeText(context, "You cannot swap with your own post", Toast.LENGTH_SHORT).show()
            return
        }

        // Get current user's skills to include in notification
        firestore.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val currentUserName = document.getString("name") ?: "A user"

                    // Create notification
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

                    // Save notification to Firestore
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
}