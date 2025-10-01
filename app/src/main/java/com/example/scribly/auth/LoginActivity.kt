package com.example.scribly.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.scribly.MainActivity
import com.example.scribly.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.example.scribly.BuildConfig
import com.example.scribly.Model.UserData
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var imageUrl: Uri? = null

    // Email validation regex pattern
    private val emailPattern = "[a-zA-Z0-9._%+-]+@[gmail - email-yahoo]+\\.(com|org|net|edu|gov|mil|biz|info|io|me|co|in|us|uk|ca|au)"

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageUrl = it
            Glide.with(this)
                .load(it)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.userImage)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Initialize Cloudinary
        val config = mapOf(
            "cloud_name" to BuildConfig.cloud_name,
            "api_key" to BuildConfig.cloud_api_key,
            "api_secret" to BuildConfig.cloud_api_secret
        )
        MediaManager.init(this, config)

        val action = intent.getStringExtra("action")

        if (action == "login") {
            binding.loginLayout.visibility = View.VISIBLE
            binding.registerLayout.visibility = View.GONE

            binding.login2.setOnClickListener {
                val email = binding.userEmail.text.toString()
                val password = binding.userPassword.text.toString()

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Please fill all the details", Toast.LENGTH_SHORT).show()
                } else {
                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Login Failed, Please Enter correct Details", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        } else if (action == "register") {
            binding.loginLayout.visibility = View.GONE
            binding.registerLayout.visibility = View.VISIBLE

            binding.userImage.setOnClickListener {
                pickImage.launch("image/*")
            }

            binding.register2.setOnClickListener {
                val name = binding.registerName.text.toString().trim()
                val email = binding.registerEmail.text.toString().trim()
                val password = binding.registerpassword.text.toString().trim()

                // Validate all fields
                if (name.isEmpty() || email.isEmpty() || password.isEmpty() || imageUrl == null) {
                    Toast.makeText(this, "Please fill all the details and upload an image", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Validate email format
                if (!email.matches(emailPattern.toRegex())) {
                    Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Proceed with registration
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        user?.let {
                            val userId = user.uid
                            val userData = UserData(name, email)
                            database.getReference("users").child(userId).setValue(userData)
                                .addOnSuccessListener {
                                    if (imageUrl != null) {
                                        uploadImageToCloudinary(userId)
                                    } else {
                                        // Clear the registration form
                                        binding.registerName.text?.clear()
                                        binding.registerEmail.text?.clear()
                                        binding.registerpassword.text?.clear()
                                        binding.userImage.setImageResource(0) // Clear the image

                                        // Sign out the user after registration
                                        auth.signOut()

                                        // Show success message and redirect to login
                                        Toast.makeText(this, "Registration Successful. Please log in.", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this, LoginActivity::class.java)
                                        intent.putExtra("action", "login")
                                        startActivity(intent)
                                        finish()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error saving user data: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        // Handle registration failure
                        val exception = task.exception
                        if (exception is FirebaseAuthUserCollisionException) {
                            // Email is already in use
                            Toast.makeText(this, "Email is already registered. Please log in.", Toast.LENGTH_SHORT).show()
                        } else {
                            // Other registration errors
                            Toast.makeText(this, "Registration Failed: ${exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun uploadImageToCloudinary(userId: String) {
        imageUrl?.let { uri ->
            MediaManager.get().upload(uri)
                .unsigned("profile_image") // Replace with your Cloudinary upload preset
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        // Upload started
                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        // Upload in progress
                    }

                    override fun onSuccess(requestId: String, resultData: Map<Any?, Any?>) {
                        val imageUrl = resultData["url"].toString()
                        database.getReference("users").child(userId).child("profileImage").setValue(imageUrl)
                            .addOnSuccessListener {
                                // Clear the registration form
                                binding.registerName.text?.clear()
                                binding.registerEmail.text?.clear()
                                binding.registerpassword.text?.clear()
                                binding.userImage.setImageResource(0) // Clear the image

                                // Sign out the user after registration
                                auth.signOut()

                                // Show success message and redirect to login
                                Toast.makeText(this@LoginActivity, "Registration Successful. Please log in.", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@LoginActivity, LoginActivity::class.java)
                                intent.putExtra("action", "login")
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this@LoginActivity, "Error saving image URL: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        Toast.makeText(this@LoginActivity, "Image upload failed: ${error.description}", Toast.LENGTH_SHORT).show()
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        // Upload rescheduled
                    }
                })
                .dispatch()
        }
    }
}