package com.example.scribly

import android.content.Intent
import android.os.Bundle
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.scribly.Model.BlogItemModel
import com.example.scribly.activity.AddArticleActivity
import com.example.scribly.activity.SavedActivity
import com.example.scribly.adapter.BlogAdapter
import com.example.scribly.auth.ProfileActivity
import com.example.scribly.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var databaseReference: DatabaseReference
    private val blogItems = mutableListOf<BlogItemModel>()
    private lateinit var blogAdapter: BlogAdapter
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference.child("blogs")

        // Set up RecyclerView with Adapter
        blogAdapter = BlogAdapter(blogItems)
        binding.blogRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = blogAdapter
        }

        // Load blogs from Firebase
        fetchBlogPosts()

        // Load user profile image if logged in
        auth.currentUser?.uid?.let { loadUserProfileImage(it) }

        // Set click listeners
        binding.saveArticalButton.setOnClickListener {
            startActivity(Intent(this, SavedActivity::class.java))
        }

        binding.profileImage.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.floatingArticleButton.setOnClickListener {
            startActivity(Intent(this, AddArticleActivity::class.java))
        }

        // Implement search functionality
        binding.searchBlog.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterBlogs(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterBlogs(newText)
                return true
            }
        })
    }

    private fun fetchBlogPosts() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                blogItems.clear()
                for (data in snapshot.children) {
                    val blogItem = data.getValue(BlogItemModel::class.java)
                    blogItem?.let { blogItems.add(it) }
                }
                blogItems.reverse()  // Show latest posts first
                blogAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Failed to load blogs", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterBlogs(query: String?) {
        val filteredList = if (query.isNullOrBlank()) {
            blogItems
        } else {
            blogItems.filter {
                it.heading?.contains(query, ignoreCase = true) == true ||
                        it.post?.contains(query, ignoreCase = true) == true ||
                        it.userName?.contains(query, ignoreCase = true) == true
            }.toMutableList()
        }
        blogAdapter.updateData(filteredList)
    }

    private fun loadUserProfileImage(userId: String) {
        val userReference = FirebaseDatabase.getInstance().reference.child("users").child(userId)
        userReference.child("profileImage").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(String::class.java)?.let { profileImageUrl ->
                    Glide.with(this@MainActivity)
                        .load(profileImageUrl.replace("http://", "https://"))  // Ensure HTTPS
                        .error(R.drawable.love) // Fallback image
                        .into(binding.profileImage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error loading image", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
