package com.example.scribly.activity

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scribly.Model.BlogItemModel
import com.example.scribly.R
import com.example.scribly.adapter.BlogAdapter
import com.example.scribly.databinding.ActivityAddArticleBinding
import com.example.scribly.databinding.ActivitySavedBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SavedActivity : AppCompatActivity() {
    private val binding: ActivitySavedBinding by lazy{
        ActivitySavedBinding.inflate(layoutInflater)
    }

    private val savedBlogArticles = mutableListOf<BlogItemModel>()
    private lateinit var blogAdapter: BlogAdapter
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        //initialize blogAdapter
        blogAdapter = BlogAdapter(savedBlogArticles.filter { it.isSaved }.toMutableList())

        val recylerview =binding.savedArticle
        recylerview.adapter =blogAdapter
        recylerview.layoutManager =LinearLayoutManager(this)

        val userId =auth.currentUser?.uid
        if(userId !=null){

            val userReference =FirebaseDatabase.getInstance()
                .getReference("users").child(userId).child("saveBlogPosts")

            userReference.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (postSnapshot in snapshot.children){
                        val postId =postSnapshot.key
                        val isSaved =postSnapshot.value as Boolean
                        if(postId !=null && isSaved){
                            //fetch corresponding blog item on postid using a coroutine
                            CoroutineScope(Dispatchers.IO).launch {
                                val blogItem =fetchBlogItem(postId)
                                if(blogItem !=null){
                                    savedBlogArticles.add(blogItem)

                                    launch (Dispatchers.Main){
                                        blogAdapter.updateData(savedBlogArticles)
                                    }
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }

        val backButton =findViewById<ImageButton>(R.id.backbtn3)
        backButton.setOnClickListener {
            finish()
        }
    }

    private suspend fun fetchBlogItem(postId: String): BlogItemModel? {
        val blogReference = FirebaseDatabase.getInstance()
            .getReference("blogs")
        return try {
            val dataSnapshot =blogReference.child(postId).get(). await()
            val blogData =dataSnapshot.getValue(BlogItemModel::class.java)
            blogData
        }catch (e: Exception){
            null
        }
    }
}