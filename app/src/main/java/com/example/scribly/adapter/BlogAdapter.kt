package com.example.scribly.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.scribly.Model.BlogItemModel
import com.example.scribly.R
import com.example.scribly.activity.ReadMoreActivity
import com.example.scribly.databinding.BlogItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class BlogAdapter(private val item: MutableList<BlogItemModel>) : RecyclerView.Adapter<BlogAdapter.BlogViewHolder>() {

    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = BlogItemBinding.inflate(inflater, parent, false)
        return BlogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {
        val blogItem = item[position]
        holder.bind(blogItem)
    }

    override fun getItemCount(): Int = item.size

    inner class BlogViewHolder(private val binding: BlogItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(blogItemModel: BlogItemModel) {
            val postId = blogItemModel.postId ?: return
            val context = binding.root.context

            binding.intro.text = blogItemModel.heading
            binding.intro2.text = blogItemModel.post
            binding.userId.text = blogItemModel.userName
            binding.date.text = blogItemModel.date
            binding.likes.text = blogItemModel.likeCount.toString()

            var imageUrl = blogItemModel.imageUrl ?: ""
            if (imageUrl.startsWith("http://")) {
                imageUrl = imageUrl.replace("http://", "https://")
            }

            Glide.with(binding.otherimage.context)
                .load(imageUrl.ifBlank { null })
                .placeholder(R.drawable.register)
                .error(R.drawable.love)
                .into(binding.otherimage)

            binding.readmore.setOnClickListener {
                val intent = Intent(context, ReadMoreActivity::class.java)
                intent.putExtra("blogItem", blogItemModel)
                context.startActivity(intent)
            }

            val postLikeReference = databaseReference.child("blogs").child(postId).child("likes")

            // Check if the current user has liked the post
            postLikeReference.child(currentUser?.uid ?: "").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val liked = snapshot.exists()
                    updateLikeButtonImage(binding, liked)
                }

                override fun onCancelled(error: DatabaseError) {}
            })

            binding.heart.setOnClickListener {
                if (currentUser != null) {
                    handleLikeButtonClicked(postId, blogItemModel, binding)
                } else {
                    Toast.makeText(context, "You need to login first", Toast.LENGTH_SHORT).show()
                }
            }

            val postSaveReference = databaseReference.child("users").child(currentUser?.uid ?: "").child("saveBlogPosts")

            postSaveReference.child(postId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val isSaved = snapshot.exists()
                    blogItemModel.isSaved = isSaved
                    updateSaveButtonImage(binding, isSaved)
                }

                override fun onCancelled(error: DatabaseError) {}
            })

            binding.save.setOnClickListener {
                if (currentUser != null) {
                    handleSaveButtonClicked(postId, blogItemModel, binding)
                } else {
                    Toast.makeText(context, "You need to login first", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleLikeButtonClicked(postId: String, blogItemModel: BlogItemModel, binding: BlogItemBinding) {
        val userReference = databaseReference.child("users").child(currentUser!!.uid)
        val postLikeReference = databaseReference.child("blogs").child(postId).child("likes")

        postLikeReference.child(currentUser!!.uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    userReference.child("likes").child(postId).removeValue().addOnSuccessListener {
                        postLikeReference.child(currentUser!!.uid).removeValue()
                        blogItemModel.likedBy?.remove(currentUser!!.uid)
                        updateLikeButtonImage(binding, false)

                        val newLikeCount = blogItemModel.likeCount - 1
                        blogItemModel.likeCount = newLikeCount
                        databaseReference.child("blogs").child(postId).child("likeCount").setValue(newLikeCount)
                    }
                } else {
                    userReference.child("likes").child(postId).setValue(true).addOnSuccessListener {
                        postLikeReference.child(currentUser!!.uid).setValue(true)
                        blogItemModel.likedBy?.add(currentUser!!.uid)
                        updateLikeButtonImage(binding, true)

                        val newLikeCount = blogItemModel.likeCount + 1
                        blogItemModel.likeCount = newLikeCount
                        databaseReference.child("blogs").child(postId).child("likeCount").setValue(newLikeCount)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun handleSaveButtonClicked(postId: String, blogItemModel: BlogItemModel, binding: BlogItemBinding) {
        val userReference = databaseReference.child("users").child(currentUser!!.uid).child("saveBlogPosts")

        userReference.child(postId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    userReference.child(postId).removeValue().addOnSuccessListener {
                        blogItemModel.isSaved = false
                        updateSaveButtonImage(binding, false)
                        Toast.makeText(binding.root.context, "Blog Unsaved!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    userReference.child(postId).setValue(true).addOnSuccessListener {
                        blogItemModel.isSaved = true
                        updateSaveButtonImage(binding, true)
                        Toast.makeText(binding.root.context, "Blog Saved!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateLikeButtonImage(binding: BlogItemBinding, liked: Boolean) {
        binding.heart.setImageResource(if (liked) R.drawable.heart_fill else R.drawable.like)
    }

    private fun updateSaveButtonImage(binding: BlogItemBinding, saved: Boolean) {
        binding.save.setImageResource(if (saved) R.drawable.save_fill else R.drawable.red_save)
    }

    fun updateData(savedBlogArticles: MutableList<BlogItemModel>) {
        item.clear()
        item.addAll(savedBlogArticles)
        notifyDataSetChanged()

    }
}
