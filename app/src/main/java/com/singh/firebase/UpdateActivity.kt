package com.singh.firebase

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.identity.util.UUID
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.singh.firebase.databinding.ActivityUpdateBinding
import com.squareup.picasso.Picasso

class UpdateActivity : AppCompatActivity() {
    lateinit var updateBinding: ActivityUpdateBinding
    var database: FirebaseDatabase = FirebaseDatabase.getInstance()
    var myReference: DatabaseReference = database.reference.child("MyUsers")
    lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    var imageUri: Uri?=null
    val firebaseStorage : FirebaseStorage = FirebaseStorage.getInstance()
    val storageReference : StorageReference = firebaseStorage.reference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        updateBinding = ActivityUpdateBinding.inflate(layoutInflater)
        setContentView(updateBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        registerActivityForResult()

       getAndSetData()

        updateBinding.buttonAddUserUpdate.setOnClickListener {

                updateData()
        }

        updateBinding.userProfileImageUpdate.setOnClickListener {

            getImageFromGallery()

        }

    }

    fun getAndSetData(){
        val name= intent.getStringExtra("userName")
        val age= intent.getIntExtra("userAge",0).toString()
        val email= intent.getStringExtra("userEmail")
        val imageUrl= intent.getStringExtra("userProfileImageUrl")
        val imageName= intent.getStringExtra("userProfileImageName")

        updateBinding.editTextAddNameUpdate.setText(name)
        updateBinding.editTextAddAgeUpdate.setText(age)
        updateBinding.editTextAddEmailUpdate.setText(email)
        Picasso.get().load(imageUrl).into(updateBinding.userProfileImageUpdate)


    }

    fun updateData() {
        val updatedName = updateBinding.editTextAddNameUpdate.text.toString()
        val updatedAge = updateBinding.editTextAddAgeUpdate.text.toString().toInt()
        val updatedEmail = updateBinding.editTextAddEmailUpdate.text.toString()
        val userId = intent.getStringExtra("userId").toString()

        val userMap = mutableMapOf<String, Any>()
        userMap["userId"] = userId
        userMap["userName"] = updatedName
        userMap["userAge"] = updatedAge
        userMap["userEmail"] = updatedEmail

        myReference.child(userId).updateChildren(userMap).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(applicationContext, "The user has been updated", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }


        }
    }
    fun getImageFromGallery(){

        val permission=if(Build.VERSION.SDK_INT>= 33){
            android.Manifest.permission.READ_MEDIA_IMAGES
        }else{
            android.Manifest.permission.READ_EXTERNAL_STORAGE

        }

        if(ContextCompat.checkSelfPermission(this,permission)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(permission),1)

        }
        else{
            val intent = Intent(Intent.ACTION_PICK)
            intent.type="image/*"
            intent.action=Intent.ACTION_GET_CONTENT
            activityResultLauncher.launch(intent)
        }
    }

    fun registerActivityForResult(){

        activityResultLauncher=registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback { result->

                val resultCode=result.resultCode
                val imageData=result.data
                if(resultCode== RESULT_OK && imageData!=null) {
                    imageUri = imageData.data
                    updateBinding.userProfileImageUpdate.setImageURI(imageUri)
                    imageUri?.let {
                        Picasso.get().load(it).into(updateBinding.userProfileImageUpdate)
                    }

                }


            })

    }

    fun updateUser(url:String,imageName:String){
        val name:String = updateBinding.editTextAddNameUpdate.text.toString()
        val age: Int = updateBinding.editTextAddAgeUpdate.text.toString().toInt()
        val email: String = updateBinding.editTextAddEmailUpdate.text.toString()

        val id: String = myReference.push().key.toString()
        val userMap= mutableMapOf<String, Any>()
        userMap["userId"]=id
        userMap["userName"]=name
        userMap["userAge"]=age
        userMap["userEmail"]=email
        userMap["userProfileImageUrl"]=url
        userMap["userProfileImageName"]=imageName
        myReference.child(id).setValue(userMap).addOnCompleteListener {task->
            if (task.isSuccessful){
                Toast.makeText(applicationContext,"The new user is added to the database",Toast.LENGTH_SHORT).show()
                updateBinding.buttonAddUserUpdate.isClickable = true
                updateBinding.progressBarAddUserUpdate.visibility = View.INVISIBLE
                finish()
            }
            else{
                Toast.makeText(applicationContext,task.exception.toString(),Toast.LENGTH_SHORT).show()
            }
        }

    }

    fun uploadPhoto() {

        updateBinding.buttonAddUserUpdate.isClickable = false
        updateBinding.progressBarAddUserUpdate.visibility = View.VISIBLE

        //UUID

        val imageName = intent.getStringExtra("userProfileImageName").toString()
        val imageReference = storageReference.child("images").child(imageName)

        imageUri?.let { uri ->

            imageReference.putFile(uri).addOnSuccessListener {

                Toast.makeText(applicationContext, "Image uploaded", Toast.LENGTH_SHORT).show()

                //downloadable url

                val myUploadedImageReference = storageReference.child("images").child(imageName)

                myUploadedImageReference.downloadUrl.addOnSuccessListener { url ->

                    val imageURL = url.toString()

                    updateUser(imageURL, imageName)

                }

            }.addOnFailureListener {

                Toast.makeText(applicationContext, it.localizedMessage, Toast.LENGTH_SHORT).show()

            }

        }
    }

}