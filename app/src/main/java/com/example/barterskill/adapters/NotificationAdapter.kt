package com.example.barterskill.adapters

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.barterskill.R
import com.example.barterskill.databinding.ItemNotificationBinding
import com.example.barterskill.models.Notification
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(
    private val notifications: List<Notification>,
    private val onAcceptClickListener: (Notification) -> Unit,
    private val onDeclineClickListener: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    private val firestore = FirebaseFirestore.getInstance()

    class NotificationViewHolder(
        val binding: ItemNotificationBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]

        // Load sender details
        firestore.collection("users").document(notification.senderId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val senderName = document.getString("name") ?: "Unknown User"

                    val base64Image = document.getString("base64ProfileImage")
                    if (!base64Image.isNullOrEmpty()) {
                        try {
                            val decodedBytes = Base64.decode(base64Image, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                            Glide.with(holder.itemView.context)
                                .load(bitmap)
                                .circleCrop()
                                .placeholder(R.drawable.ic_profile)
                                .into(holder.binding.notificationUserImageView)
                        } catch (e: Exception) {
                            Glide.with(holder.itemView.context)
                                .load(R.drawable.ic_profile)
                                .circleCrop()
                                .into(holder.binding.notificationUserImageView)
                        }
                    }

                    // Format the notification message based on type
                    when (notification.type) {
                        "swap_request" -> {
                            val message = "$senderName wants to swap ${notification.details["senderSkill"]} for ${notification.details["recipientSkill"]}"
                            holder.binding.notificationTextView.text = message
                        }
                        "swap_accepted" -> {
                            holder.binding.notificationTextView.text = "$senderName accepted your swap request"
                            holder.binding.acceptButton.visibility = android.view.View.GONE
                            holder.binding.declineButton.visibility = android.view.View.GONE
                        }
                        "swap_declined" -> {
                            holder.binding.notificationTextView.text = "$senderName declined your swap request"
                            holder.binding.acceptButton.visibility = android.view.View.GONE
                            holder.binding.declineButton.visibility = android.view.View.GONE
                        }
                    }
                }
            }

        // Format timestamp
        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
        holder.binding.notificationTimeTextView.text = sdf.format(notification.timestamp)

        // Handle button clicks
        if (notification.type == "swap_request") {
            holder.binding.acceptButton.setOnClickListener {
                onAcceptClickListener(notification)
            }
            holder.binding.declineButton.setOnClickListener {
                onDeclineClickListener(notification)
            }
        }
    }

    override fun getItemCount(): Int = notifications.size
}