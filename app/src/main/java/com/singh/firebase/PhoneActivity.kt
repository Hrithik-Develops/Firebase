package com.singh.firebase

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.singh.firebase.databinding.ActivityPhoneBinding
import java.util.concurrent.TimeUnit

class PhoneActivity : AppCompatActivity() {
    lateinit var phoneBinding: ActivityPhoneBinding
    lateinit var mCallBacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    var verificationCode= ""
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        phoneBinding = ActivityPhoneBinding.inflate(layoutInflater)
        setContentView(phoneBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        phoneBinding.buttonSendOtp.setOnClickListener {

            val userPhoneNumber = phoneBinding.editTextPhone.text.toString()
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(userPhoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this@PhoneActivity)
                .setCallbacks(mCallBacks)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)

            val intent = Intent(this, OtpActivity::class.java).apply {
                putExtra("verificationCode", verificationCode)
                putExtra("userPhoneNumber", userPhoneNumber)
            }
            startActivity(intent)
        }

        mCallBacks=object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                TODO("Not yet implemented")
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                TODO("Not yet implemented")
            }

            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(p0, p1)
                verificationCode=p0
            }



        }

    }


}