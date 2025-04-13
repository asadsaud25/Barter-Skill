package com.example.barterskill.fragments.main

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.barterskill.R
import com.example.barterskill.adapters.SkillAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var profileImageView: ImageView
    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var cityTextView: TextView
    private lateinit var skillsRecyclerView: RecyclerView
    private lateinit var editProfileButton: Button
    private lateinit var logoutButton: Button

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize views
        profileImageView = view.findViewById(R.id.profileImageView)
        nameTextView = view.findViewById(R.id.nameTextView)
        emailTextView = view.findViewById(R.id.emailTextView)
        cityTextView = view.findViewById(R.id.cityTextView)
        skillsRecyclerView = view.findViewById(R.id.skillsRecyclerView)
        editProfileButton = view.findViewById(R.id.editProfileButton)
        logoutButton = view.findViewById(R.id.logoutButton)

        skillsRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        // Set up edit profile button
        editProfileButton.setOnClickListener {
            findNavController().navigate(R.id.editProfileFragment)
        }

        // Set up logout button
        logoutButton.setOnClickListener {

            firebaseAuth.signOut()
            activity?.finish()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val userId = firebaseAuth.currentUser?.uid ?: return

        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    nameTextView.text = document.getString("name")
                    emailTextView.text = document.getString("email")
                    cityTextView.text = document.getString("city")

                    // Load Base64 profile image
                    val base64Image = document.getString("base64ProfileImage")
                    if (!base64Image.isNullOrEmpty()) {
                        try {
                            val decodedBytes = Base64.decode(base64Image, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                            Glide.with(this)
                                .load(bitmap)
                                .circleCrop()
                                .placeholder(R.drawable.ic_profile)
                                .into(profileImageView)
                        } catch (e: Exception) {
                            Glide.with(this)
                                .load(R.drawable.ic_profile)
                                .circleCrop()
                                .into(profileImageView)
                        }
                    } else {
                        Glide.with(this)
                            .load(R.drawable.ic_profile)
                            .circleCrop()
                            .into(profileImageView)
                    }

                    // Load skills (implement as needed)
                    val skills = document.get("skills") as? List<String>
                    // Add RecyclerView adapter logic here if needed
                    if (skills != null) {
                        val adapter = SkillAdapter(skills)
                        skillsRecyclerView.adapter = adapter
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load user profile", Toast.LENGTH_SHORT).show()
                // Handle error if needed
            }
    }
}