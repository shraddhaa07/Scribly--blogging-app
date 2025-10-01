package com.example.scribly.auth

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.scribly.R
import com.example.scribly.activity.WelcomeActivity
import com.example.scribly.activity.YourArticleActivity
import com.example.scribly.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {
    private val binding: ActivityProfileBinding by lazy{
        ActivityProfileBinding.inflate(layoutInflater)
    }

    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private fun loadUserProfileData(userId: String) {
        val userReference = databaseReference.child(userId)

        // Load user profile picture
        userReference.child("profileImage").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profileImageUrl = snapshot.getValue(String::class.java)

                Log.d("ProfileActivity", "Profile Image URL: $profileImageUrl")

                var imageUrl = profileImageUrl ?: "" // Default to empty string if null

                // Convert HTTP to HTTPS if necessary
                if (imageUrl.startsWith("http://")) {
                    imageUrl = imageUrl.replace("http://", "https://")
                }

                Glide.with(this@ProfileActivity)
                    .load(imageUrl.ifBlank { null }) // Prevents empty string errors
                    .placeholder(R.drawable.register) // Placeholder image
                    .error(R.drawable.love) // Error image in case of failure
                    .into(binding.profileImage2)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileActivity", "Failed to load profile image: ${error.message}")
            }
        })

        userReference.child("name").addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val userName =snapshot.getValue(String::class.java)
                if (userId !=null){
                    binding.name.text =userName
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.feedbackBtn.setOnClickListener {
            showFeedbackDialog()
        }


        //to logout
        binding.logoutBtn.setOnClickListener{
            auth.signOut()
            //navigate
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }
        //to your article
        binding.yourarticleBtn.setOnClickListener {
            startActivity(Intent(this, YourArticleActivity::class.java))
        }

        //initialize firebase
        auth =FirebaseAuth.getInstance()
        databaseReference =FirebaseDatabase.getInstance().reference.child("users")

        val userId =auth.currentUser?.uid

        if(userId !=null){
            loadUserProfileData(userId)
        }

    }

    private fun showFeedbackDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Feedback")

        // Create an input field
        val input = EditText(this)
        input.hint = "Write your feedback here..."
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        input.setPadding(50, 20, 50, 20)

        builder.setView(input)

        // Set dialog buttons
        builder.setPositiveButton("Submit") { dialog, _ ->
            val feedbackText = input.text.toString().trim()
            if (feedbackText.isNotEmpty()) {
                Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show()
                // Optionally: Save feedback to Firebase (implement if needed)
                saveFeedbackToFirebase(feedbackText)
            } else {
                Toast.makeText(this, "Please enter feedback!", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        // Show the dialog
        builder.show()
    }

    private fun saveFeedbackToFirebase(feedback: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val feedbackRef = FirebaseDatabase.getInstance().reference.child("feedback").child(userId)

            val feedbackData = hashMapOf(
                "feedback" to feedback,
                "timestamp" to System.currentTimeMillis()
            )

            feedbackRef.setValue(feedbackData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Feedback saved successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save feedback!", Toast.LENGTH_SHORT).show()
                }
        }

    }
}