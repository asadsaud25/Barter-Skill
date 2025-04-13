package com.example.barterskill.fragments.main

import android.content.Intent
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
import com.example.barterskill.AuthActivity
import com.example.barterskill.R
import com.example.barterskill.adapters.SkillAdapter
import com.example.barterskill.databinding.FragmentProfileBinding
import com.example.barterskill.fragments.auth.LoginFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView
        binding.skillsRecyclerView.layoutManager = LinearLayoutManager(
            context, LinearLayoutManager.HORIZONTAL, false
        )

        // Set up edit profile button
        binding.editProfileButton.setOnClickListener {
            findNavController().navigate(R.id.editProfileFragment)
        }

        // Set up logout button
//        binding.logoutButton.setOnClickListener {
//            firebaseAuth.signOut()
//            activity?.finish()
//        }
        binding.logoutButton.setOnClickListener {
            firebaseAuth.signOut()
            val intent = Intent(requireContext(), AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }


        loadUserProfile()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadUserProfile() {
        val userId = firebaseAuth.currentUser?.uid ?: return

        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    binding.nameTextView.text = document.getString("name")
                    binding.emailTextView.text = document.getString("email")
                    binding.cityTextView.text = document.getString("city")

                    val base64Image = document.getString("base64ProfileImage")
                    if (!base64Image.isNullOrEmpty()) {
                        try {
                            val decodedBytes = Base64.decode(base64Image, Base64.DEFAULT)
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

                    val skills = document.get("skills") as? List<String>
                    if (skills != null) {
                        val adapter = SkillAdapter(skills)
                        binding.skillsRecyclerView.adapter = adapter
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load user profile", Toast.LENGTH_SHORT).show()
            }
    }
}