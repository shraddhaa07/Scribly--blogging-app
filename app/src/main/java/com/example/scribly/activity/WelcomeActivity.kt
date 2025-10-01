package com.example.scribly.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.scribly.MainActivity
import com.example.scribly.R
import com.example.scribly.auth.LoginActivity
import com.example.scribly.databinding.ActivityWelcomeBinding
import com.google.firebase.auth.FirebaseAuth

class WelcomeActivity : AppCompatActivity() {
    private val binding: ActivityWelcomeBinding by lazy {
        ActivityWelcomeBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        binding.loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("action", "login")
            startActivity(intent)
            finish()
        }

        binding.registerButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("action", "register")
            startActivity(intent)
            finish()
        }

    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}