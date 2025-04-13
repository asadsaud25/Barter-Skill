package com.example.barterskill.fragments.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.barterskill.R
import com.example.barterskill.adapters.NotificationAdapter
import com.example.barterskill.models.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

class NotificationsFragment : Fragment() {

    private lateinit var notificationsRecyclerView: RecyclerView
    private lateinit var emptyText: TextView
    private lateinit var notificationAdapter: NotificationAdapter
    private val notificationsList = mutableListOf<Notification>()

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)

        notificationsRecyclerView = view.findViewById(R.id.notificationsRecyclerView)
        emptyText = view.findViewById(R.id.emptyNotificationsText)

        notificationsRecyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize adapter
        notificationAdapter = NotificationAdapter(
            notificationsList,
            { notification -> acceptSwapRequest(notification) },
            { notification -> declineSwapRequest(notification) }
        )
        notificationsRecyclerView.adapter = notificationAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadNotifications()
    }

    override fun onResume() {
        super.onResume()
        loadNotifications()
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
                    emptyText.visibility = View.VISIBLE
                    notificationsRecyclerView.visibility = View.GONE
                } else {
                    emptyText.visibility = View.GONE
                    notificationsRecyclerView.visibility = View.VISIBLE

                    for (document in documents) {
                        val notification = document.toObject(Notification::class.java).copy(id = document.id)
                        notificationsList.add(notification)
                    }
                }

                notificationAdapter.notifyDataSetChanged()

                // Mark notifications as read
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

        // Create a notification for the sender that their request was accepted
        val acceptNotification = hashMapOf(
            "type" to "swap_accepted",
            "senderId" to currentUser.uid,
            "recipientId" to notification.senderId,
            "postId" to notification.postId,
            "details" to notification.details,
            "timestamp" to Date(),
            "read" to false
        )

        firestore.collection("notifications")
            .add(acceptNotification)
            .addOnSuccessListener {
                // Update the original notification as processed
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

    private fun declineSwapRequest(notification: Notification) {
        val currentUser = firebaseAuth.currentUser ?: return

        // Create a notification for the sender that their request was declined
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
                // Update the original notification as processed
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