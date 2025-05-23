package com.example.barterskill.fragments.main

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.barterskill.R
import com.example.barterskill.databinding.FragmentEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.InputStream

class EditProfileFragment : Fragment() {

    private lateinit var _binding: FragmentEditProfileBinding
    private val binding get() = _binding

    private var imageUri: Uri? = null

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                imageUri = uri
                Glide.with(this)
                    .load(uri)
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .into(binding.editProfileImageView)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)

        loadUserData()

        binding.changePhotoButton.setOnClickListener {
            openImageChooser()
        }

        // Set up save button
        binding.saveButton.setOnClickListener {
            saveUserData()
        }

        return binding.root
    }

    private fun loadUserData() {
        val userId = firebaseAuth.currentUser?.uid ?: return

        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    binding.nameEditText.setText(document.getString("name"))
                    binding.cityEditText.setText(document.getString("city"))
                    binding.phoneEditText.setText(document.getString("phone"))

                    val skills = document.get("skills") as? List<String>
                    if (!skills.isNullOrEmpty()) {
                        binding.skillsEditText.setText(skills.joinToString(", "))
                    }

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
                                .into(binding.editProfileImageView)
                        } catch (e: Exception) {
                            Glide.with(this)
                                .load(R.drawable.ic_profile)
                                .circleCrop()
                                .into(binding.editProfileImageView)
                        }
                    } else {
                        Glide.with(this)
                            .load(R.drawable.ic_profile)
                            .circleCrop()
                            .into(binding.editProfileImageView)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error loading profile: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserData() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val name = binding.nameEditText.text.toString().trim()
        val city = binding.cityEditText.text.toString().trim()
        val phone = binding.phoneEditText.text.toString().trim()

        // Parse skills string into list
        val skillsString = binding.skillsEditText.text.toString().trim()
        val skillsList = skillsString.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        if (name.isEmpty()) {
            binding.nameEditText.error = "Name cannot be empty"
            return
        }

        val userUpdates = hashMapOf<String, Any>(
            "name" to name,
            "city" to city,
            "phone" to phone,
            "skills" to skillsList
        )

        // If there's a new image, encode it to Base64
        imageUri?.let { uri ->
            val base64Image = encodeImageToBase64(uri)
            if (!base64Image.isNullOrEmpty()) {
                userUpdates["base64ProfileImage"] = base64Image
            }
        }

        updateUserInFirestore(userId, userUpdates)
    }

    private fun encodeImageToBase64(uri: Uri): String? {
        return try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) Base64.encodeToString(bytes, Base64.DEFAULT) else null
        } catch (e: Exception) {
            Log.e("Base64Encoding", "Error encoding image: ${e.message}")
            null
        }
    }

    private fun updateUserInFirestore(userId: String, updates: Map<String, Any>) {
        firestore.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error updating profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }
}