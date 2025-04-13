package com.example.barterskill.fragments.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.barterskill.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class CreatePostFragment : Fragment() {

    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var offerSkillEditText: EditText
    private lateinit var wantSkillEditText: EditText
    private lateinit var postButton: Button

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_post, container, false)

        titleEditText = view.findViewById(R.id.titleEditText)
        descriptionEditText = view.findViewById(R.id.descriptionEditText)
        offerSkillEditText = view.findViewById(R.id.offerSkillEditText)
        wantSkillEditText = view.findViewById(R.id.wantSkillEditText)
        postButton = view.findViewById(R.id.postButton)

        postButton.setOnClickListener {
            createPost()
        }

        return view
    }

    private fun createPost() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val title = titleEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()
        val offerSkill = offerSkillEditText.text.toString().trim()
        val wantSkill = wantSkillEditText.text.toString().trim()

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