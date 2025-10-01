package com.example.scribly.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.scribly.Model.BlogItemModel
import com.example.scribly.Model.UserData
import com.example.scribly.databinding.ActivityAddArticleBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date

class AddArticleActivity : AppCompatActivity() {
    private val binding: ActivityAddArticleBinding by lazy {
        ActivityAddArticleBinding.inflate(layoutInflater)
    }

    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("blogs")
    private val userReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        binding.backbtn.setOnClickListener{
            finish()
        }

        binding.addblogBtn.setOnClickListener {
            val title = binding.heading.editText?.text.toString().trim()
            val description = binding.body.editText?.text.toString().trim()

            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please fill your blog", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Get current user who posted the blog
            val user: FirebaseUser? = auth.currentUser

            if (user != null) {
                val userId = user.uid

                // Fetch the user name and image from the database
                userReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userData = snapshot.getValue(UserData::class.java)
                        if (userData != null) {
                            val userNameFromDB = userData.name
                            var userImageurlFromCloud = userData.profileImage ?: ""

                            // Ensure URL uses HTTPS
                            if (userImageurlFromCloud.startsWith("http://")) {
                                userImageurlFromCloud = userImageurlFromCloud.replace("http://", "https://")
                            }

                            // Log the URL for debugging
                            Log.d("AddArticleActivity", "User Image URL: $userImageurlFromCloud")

                            // Create BlogItemModel
                            val currentDate = SimpleDateFormat("yyyy-MM-dd").format(Date())
                            val blogItem = BlogItemModel(
                                title,
                                userNameFromDB,
                                currentDate,
                                description,
                                userId,
                                0,
                                userImageurlFromCloud
                            )

                            // Generate unique key for blogs
                            val key = databaseReference.push().key
                            if (key != null) {

                                blogItem.postId = key
                                val blogReference = databaseReference.child(key)
                                blogReference.setValue(blogItem).addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        Toast.makeText(this@AddArticleActivity, "Blog added successfully", Toast.LENGTH_SHORT).show()
                                        finish()
                                    } else {
                                        Toast.makeText(this@AddArticleActivity, "Failed to add blog", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(this@AddArticleActivity, "User data not found", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@AddArticleActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            }
        }
    }
}