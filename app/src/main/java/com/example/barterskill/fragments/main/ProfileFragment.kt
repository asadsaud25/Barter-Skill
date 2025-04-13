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
import com.example.barterskill.adapters.SkillAdapter
import com.example.barterskill.databinding.FragmentProfileBinding
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
                    binding.etName.text = document.getString("name")
                    binding.etEmail.text = document.getString("email")
                    binding.etfulladdr.text = document.getString("address")
                    binding.etpincode.text = document.getString("pincode")

                    val base64Image = document.getString("base64ProfileImage")
                    if (!base64Image.isNullOrEmpty()) {
                        try {
                            val decodedBytes = Base64.decode(base64Image, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                            Glide.with(this)
                                .load(bitmap)
                                .circleCrop()
                                .placeholder(R.drawable.ic_profile)
                                .into(binding.ivProfilePic)
                        } catch (e: Exception) {
                            Glide.with(this)
                                .load(R.drawable.ic_profile)
                                .circleCrop()
                                .into(binding.ivProfilePic)
                        }
                    } else {
                        Glide.with(this)
                            .load(R.drawable.ic_profile)
                            .circleCrop()
                            .into(binding.ivProfilePic)
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