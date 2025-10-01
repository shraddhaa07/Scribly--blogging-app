package com.example.scribly.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.scribly.Model.BlogItemModel
import com.example.scribly.R
import com.example.scribly.databinding.ActivityEditBlogBinding
import com.google.firebase.database.FirebaseDatabase

class EditBlogActivity : AppCompatActivity() {
    private val binding: ActivityEditBlogBinding by lazy{
        ActivityEditBlogBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.backbtn.setOnClickListener {
            finish()
        }
        val blogItemModel = intent.getParcelableExtra<BlogItemModel>("blogItem")

        binding.heading.editText?.setText(blogItemModel?.heading)
        binding.body.editText?.setText(blogItemModel?.post)

        binding.editblogBtn.setOnClickListener {
            val updateHeading=binding.heading.editText?.text.toString().trim()
            val updateBody=binding.body.editText?.text.toString().trim()

            if(updateHeading.isEmpty() || updateBody.isEmpty()){
                Toast.makeText(this,"Please fill all the details",Toast.LENGTH_SHORT).show()
            }else{
                blogItemModel?.heading=updateHeading
                blogItemModel?.post=updateBody

                if(blogItemModel !=null){
                    updateDataInFirebase(blogItemModel)
                }
            }
        }
    }

    private fun updateDataInFirebase(blogItemModel: BlogItemModel) {
        val databaseReference=FirebaseDatabase.getInstance().getReference("blogs")

        val postId =blogItemModel.postId

        databaseReference.child(postId).setValue(blogItemModel)
            .addOnSuccessListener {
                Toast.makeText(this,"Blog Updated Successfully",Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener{
                Toast.makeText(this,"Blog Updation Failed",Toast.LENGTH_SHORT).show()

            }
    }
}