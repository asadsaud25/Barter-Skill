package com.example.barterskill.fragments.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.barterskill.adapters.NotificationAdapter
import com.example.barterskill.databinding.FragmentNotificationsBinding
import com.example.barterskill.models.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private val notificationsList = mutableListOf<Notification>()
    private lateinit var notificationAdapter: NotificationAdapter

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView
        binding.notificationsRecyclerView.layoutManager = LinearLayoutManager(context)
        notificationAdapter = NotificationAdapter(
            notificationsList,
            { notification -> acceptSwapRequest(notification) },
            { notification -> declineSwapRequest(notification) }
        )
        binding.notificationsRecyclerView.adapter = notificationAdapter

        loadNotifications()
    }

    override fun onResume() {
        super.onResume()
        loadNotifications()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadNotifications() {
        val userId = firebaseAuth.currentUser?.uid ?: return

        firestore.collection("notifications")
            .whereEqualTo("recipientId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                notificationsList.clear()

                if (documents.isEmpty) {
                    binding.emptyNotificationsText.visibility = View.VISIBLE
                    binding.notificationsRecyclerView.visibility = View.GONE
                } else {
                    binding.emptyNotificationsText.visibility = View.GONE
                    binding.notificationsRecyclerView.visibility = View.VISIBLE

                    for (document in documents) {
                        val notification = document.toObject(Notification::class.java).copy(id = document.id)
                        notificationsList.add(notification)
                    }
                }

                notificationAdapter.notifyDataSetChanged()

                for (document in documents) {
                    if (document.getBoolean("read") == false) {
                        firestore.collection("notifications").document(document.id)
                            .update("read", true)
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error loading notifications: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun acceptSwapRequest(notification: Notification) {
        val currentUser = firebaseAuth.currentUser ?: return

        firestore.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                val senderEmail = document.getString("email") ?: "N/A"
                val senderName = document.getString("name") ?: "A user"

                val acceptNotification = hashMapOf(
                    "type" to "swap_accepted",
                    "senderId" to currentUser.uid,
                    "recipientId" to notification.senderId,
                    "postId" to notification.postId,
                    "details" to notification.details + mapOf("contactEmail" to senderEmail, "senderName" to senderName),
                    "timestamp" to Date(),
                    "read" to false
                )

                firestore.collection("notifications")
                    .add(acceptNotification)
                    .addOnSuccessListener {
                        firestore.collection("notifications").document(notification.id)
                            .update("processed", true)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Swap request accepted!", Toast.LENGTH_SHORT).show()
                                loadNotifications()
                            }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error accepting request: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to get user info: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun declineSwapRequest(notification: Notification) {
        val currentUser = firebaseAuth.currentUser ?: return

        val declineNotification = hashMapOf(
            "type" to "swap_declined",
            "senderId" to currentUser.uid,
            "recipientId" to notification.senderId,
            "postId" to notification.postId,
            "details" to notification.details,
            "timestamp" to Date(),
            "read" to false
        )

        firestore.collection("notifications")
            .add(declineNotification)
            .addOnSuccessListener {
                firestore.collection("notifications").document(notification.id)
                    .update("processed", true)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Swap request declined", Toast.LENGTH_SHORT).show()
                        loadNotifications()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error declining request: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}