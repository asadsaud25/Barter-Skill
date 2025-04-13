package com.example.barterskill.fragments.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.barterskill.databinding.FragmentCreatePostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class CreatePostFragment : Fragment() {

    private lateinit var _binding: FragmentCreatePostBinding
    private val binding get() = _binding!!

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreatePostBinding.inflate(inflater, container, false)

        binding.postButton.setOnClickListener {
            createPost()
        }

        return binding.root
    }

    private fun createPost() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val title = binding.titleEditText.text.toString().trim()
        val description = binding.descriptionEditText.text.toString().trim()
        val offerSkill = binding.offerSkillEditText.text.toString().trim()
        val wantSkill = binding.wantSkillEditText.text.toString().trim()

        if (title.isBlank() || description.isBlank() || offerSkill.isBlank() || wantSkill.isBlank()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val postMap = hashMapOf(
            "userId" to userId,
            "title" to title,
            "description" to description,
            "offerSkill" to offerSkill,
            "wantSkill" to wantSkill,
            "timestamp" to Date(),
            "active" to true
        )

        firestore.collection("posts")
            .add(postMap)
            .addOnSuccessListener {
                Toast.makeText(context, "Post created successfully", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error creating post: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}