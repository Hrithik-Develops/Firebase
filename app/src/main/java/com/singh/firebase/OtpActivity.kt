package com.singh.firebase

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.singh.firebase.databinding.ActivityOtpBinding
import java.util.concurrent.TimeUnit

class OtpActivity : AppCompatActivity() {
    lateinit var otpBinding: ActivityOtpBinding
    lateinit var verificationCode: String
    private lateinit var userPhoneNumber: String
    var auth: FirebaseAuth=FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        otpBinding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(otpBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        verificationCode = intent.getStringExtra("verificationCode") ?: ""
        userPhoneNumber = intent.getStringExtra("userPhoneNumber") ?: ""

        otpBinding.buttonVerification.setOnClickListener {
            signInWithSmsCode()
        }
    }

    private fun signInWithSmsCode() {
        val userOtp = otpBinding.editTextOtp.text.toString()
        val credential = PhoneAuthProvider.getCredential(verificationCode, userOtp)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Successfully signed in
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    // Sign in failed
                }
            }
    }
}