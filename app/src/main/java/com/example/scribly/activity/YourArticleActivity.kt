package com.example.scribly.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scribly.Model.BlogItemModel
import com.example.scribly.adapter.ArticleAdapter
import com.example.scribly.databinding.ActivityYourArticleBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class YourArticleActivity : AppCompatActivity() {
    private lateinit var blogAdapter: ArticleAdapter
    private val binding: ActivityYourArticleBinding by lazy {
        ActivityYourArticleBinding.inflate(layoutInflater)
    }
    private lateinit var databaseReference: DatabaseReference
    private val auth =FirebaseAuth.getInstance()
    private val EDIT_BLOG_POST =123


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        val currentUserId =auth.currentUser?.uid
        val recyclerView =binding.articleRecyclerview
        recyclerView.layoutManager=LinearLayoutManager(this)

        if (currentUserId !=null){
            blogAdapter= ArticleAdapter(this, emptyList(),object:ArticleAdapter.OnItemClickListener{


                override fun onEditCLick(blogItem: BlogItemModel) {
                    val intent = Intent(this@YourArticleActivity,EditBlogActivity::class.java)
                    intent.putExtra("blogItem",blogItem)
                    startActivityForResult(intent,EDIT_BLOG_POST)
                }

                override fun onDeleteCLick(blogItem: BlogItemModel) {
                    deleteBlogPost(blogItem)
                }

                override fun onReadMoreCLick(blogItem: BlogItemModel) {
                    val intent = Intent(this@YourArticleActivity,ReadMoreActivity::class.java)
                    intent.putExtra("blogItem",blogItem)
                    startActivity(intent)
                }
            })
        }
        recyclerView.adapter=blogAdapter

        //get blog data from database
        databaseReference = FirebaseDatabase.getInstance().getReference("blogs")

        databaseReference.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val blogSavedList =ArrayList<BlogItemModel>()
                //for loop to add every data to your list
                for (postSanpshot in snapshot.children){
                    val blogSaved =postSanpshot.getValue(BlogItemModel::class.java)

                    //if blog saved is not null then pass it with user id only current user can access it
                    if(blogSaved !=null && currentUserId ==blogSaved.userId){
                        blogSavedList.add(blogSaved) //any blogs saved it in list will get added to that current user's id
                    }
                }
                blogAdapter.setData(blogSavedList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@YourArticleActivity,"Error loading the data",Toast.LENGTH_SHORT).show()

            }
        })

    }

    private fun deleteBlogPost(blogItem: BlogItemModel) {
        val postId=blogItem.postId
        val blogPostReference=databaseReference.child(postId)

        //remove the blog post
        blogPostReference.removeValue()
            .addOnSuccessListener {
                Toast.makeText(this,"Blog Deleted Successfully",Toast.LENGTH_SHORT).show()
            }.addOnFailureListener{
                Toast.makeText(this,"Blog Deletion Failed",Toast.LENGTH_SHORT).show()

            }
    }
}