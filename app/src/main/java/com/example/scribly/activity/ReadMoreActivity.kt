package com.example.scribly.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.scribly.Model.BlogItemModel
import com.example.scribly.R
import com.example.scribly.databinding.ActivityReadMoreBinding

class ReadMoreActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReadMoreBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityReadMoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backbtn2.setOnClickListener{
            finish()
        }
        val blogs = intent.getParcelableExtra<BlogItemModel>("blogItem")

        if(blogs !=null){
            //retrive user data related in the title etc
            binding.topic.text = blogs.heading
            binding.BlogView.text = blogs.post
            binding.date.text = blogs.date
            binding.author.text = blogs.userName

            Log.d("ReadMoreActivity", "Image URL: ${blogs.imageUrl}")
            var imageUrl = blogs.imageUrl ?:"" //default to empty string if null
            if (imageUrl.startsWith("http://")) {
                imageUrl = imageUrl.replace("http://", "https://")
            }


            Glide.with(binding.readmoreImg.context)
                .load(imageUrl.ifBlank { null }) // Prevents empty string errors
                .placeholder(R.drawable.register) // Add a placeholder image
                .error(R.drawable.love) // Add an error image in case loading fails
                .into(binding.readmoreImg)
        }else{
            Toast.makeText(this,"Failed to load content",Toast.LENGTH_SHORT).show()
        }

    }
}