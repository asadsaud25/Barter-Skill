# 🤝 Barter Skill

**Barter Skill** is an Android-based platform that enables users to exchange skills with each other—no money involved. Whether you're a guitar player looking to learn coding, or a painter seeking dance lessons, Barter Skill helps you connect with others and grow together through skill-sharing.

---

## 🎯 Purpose

This project was built for the **Software Hackathon of AMU Hacks 4.0**—a platform where creativity meets purpose. The app was designed and developed as a real-world solution to promote **collaborative learning** and **mutual growth**.

---

## 👥 Team

This project was created by teaming up with my friends:
- **Asad Saud**
- **Efa Arif**
- **Homa Mahmood**

---

## 🚀 Features

### 🔐 Authentication
- Firebase Authentication (Email/Password login)
- Secure signup and login flows
- Profile completion page with image upload

### 🧑‍🎨 Profile Management
- Update your personal information
- Upload and edit your profile picture
- View skills you've listed or engaged with

### 🌐 Skill Explorer
- Browse skills offered by other users
- Filter and explore by interests
- Detailed post view to see skill info and user profile

### ➕ Post Creation
- Easily create posts to offer a skill you want to teach
- Add detailed descriptions and categories

### 🔔 Real-time Notifications
- Get notified when someone wants to swap skills with you
- Accept or decline requests right from the notification
- If accepted, the requester gets notified with your email to initiate the conversation

### 💬 Skill Swapping Flow
- Request a swap with just one tap
- Receive updates when requests are accepted or declined
- Maintain transparency and control over interactions

### 📥 Image Handling
- Base64-encoded profile image storage in Firestore
- Glide image loading with placeholders and error handling

### 🧭 Navigation
- Bottom navigation for seamless access to key screens
- Fully functional fragment-based navigation
- About Us and Settings pages included

---

## 🎨 UI/UX Design

The app layout is inspired by a clean and intuitive design in **Figma**. Translating the Figma design into XML required significant effort:
- Precision placement using ConstraintLayout
- Ensuring responsiveness on different screen sizes
- Matching colors, fonts, and padding exactly to design specs

---

## 🧪 Challenges We Faced

- Implementing real-time notifications using Firestore was complex, especially maintaining UI state and consistency.
- Ensuring the proper decoding and display of Base64-encoded profile images across devices took time and debugging.
- Navigation graph errors caused runtime crashes when switching fragments due to ID mismatches.
- Recreating pixel-perfect UIs from Figma to XML layout was particularly challenging, requiring many iterations and adjustments for layout behavior.

---

## 🛠 Tech Stack

- **Kotlin**
- **Firebase (Firestore, Auth, Storage)**
- **Glide** for image loading
- **Material Components** for UI
- **ConstraintLayout** for responsive design

---

## 📲 Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/barter-skill.git
2. Open in Android Studio
3. Connect Firebase project (or use existing google-services.json)
4. Build and run on an emulator or physical device

## 🙌 Contributions
Pull requests are welcome. For major changes, please open an issue first to discuss what you'd like to change.

## 📦 APK for Testing
👉 Download Barter Skill APK for testing: [barter-skill.apk](app-debug.apk)

## 📄 License
This project is licensed under the MIT License - [MIT LICENSE](LICENSE.md)

