package com.example.barterskill.fragments.auth

import android.app.Activity
import android.content.Intent
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
import com.example.barterskill.databinding.FragmentCompleteProfileBinding
import com.example.barterskill.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.InputStream

class CompleteProfileFragment : Fragment() {

    private var _binding: FragmentCompleteProfileBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                Glide.with(this)
                    .load(uri)
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .into(binding.ivProfilePic)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompleteProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide.with(this)
            .load(R.drawable.ic_profile)
            .circleCrop()
            .into(binding.ivProfilePic)

        binding.ivProfilePic.setOnClickListener {
            openImagePicker()
        }

        binding.btnLogin.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val fullAddress = binding.etfulladdr.text.toString().trim()
            val skills = binding.etSkills.text.toString().trim()
            val pincode = binding.etpincode.text.toString().trim()

            if (validateInput(name, fullAddress, skills)) {
                completeUserProfile(name, fullAddress, skills, pincode)
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun validateInput(name: String, fullAddress: String, skills: String): Boolean {
        if (name.isEmpty()) {
            binding.etName.error = "Name is required"
            return false
        }

        if (fullAddress.isEmpty()) {
            binding.etfulladdr.error = "full address is required"
            return false
        }

        if (skills.isEmpty()) {
            binding.etSkills.error = "Please enter at least one skill"
            return false
        }

        return true
    }

    private fun completeUserProfile(name: String, fullAddress: String, skills: String, pincode: String) {
        binding.progressBar.visibility = View.VISIBLE
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(requireContext(), "Authentication error. Please login again.", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_completeProfileFragment_to_loginFragment)
            return
        }

        val userId = currentUser.uid
        val userEmail = currentUser.email ?: ""
        val skillsList = skills.split(",").map { it.trim() }

        // Convert image to Base64
        val base64Image = selectedImageUri?.let { encodeImageToBase64(it) } ?: ""

        val user = User(
            id = userId,
            name = name,
            email = userEmail,
            fullAddress = fullAddress,
            skills = skillsList,
            base64ProfileImage = base64Image,
            etpincode = pincode
        )

        saveUserToFirestore(user)
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

    private fun saveUserToFirestore(user: User) {
        db.collection("users")
            .document(user.id)
            .set(user)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Profile completed successfully!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_completeProfileFragment_to_loginFragment)
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
