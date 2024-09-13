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
import com.singh.firebase.databinding.ActivityAddBinding
import com.squareup.picasso.Picasso

class AddActivity : AppCompatActivity() {
    lateinit var addBinding: ActivityAddBinding
    var database: FirebaseDatabase= FirebaseDatabase.getInstance()
    var myReference: DatabaseReference = database.reference.child("MyUsers")
    lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
     var imageUri: Uri?=null
    val firebaseStorage : FirebaseStorage = FirebaseStorage.getInstance()
    val storageReference : StorageReference = firebaseStorage.reference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        addBinding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(addBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        registerActivityForResult()


        addBinding.buttonAddUser.setOnClickListener {


          uploadPhoto()



        }

        addBinding.userProfileImage.setOnClickListener {

            getImageFromGallery()

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

        activityResultLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback { result->

                val resultCode=result.resultCode
                val imageData=result.data
                if(resultCode== RESULT_OK && imageData!=null) {
                    imageUri = imageData.data
                    addBinding.userProfileImage.setImageURI(imageUri)
                    imageUri?.let {
                        Picasso.get().load(it).into(addBinding.userProfileImage)
                    }

                }


            })

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)


        if(requestCode==1 && grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            val intent = Intent(Intent.ACTION_PICK)
            intent.type="image/*"
            intent.action=Intent.ACTION_GET_CONTENT
            activityResultLauncher.launch(intent)
        }
        else{
            Toast.makeText(applicationContext,"Permission Denied",Toast.LENGTH_SHORT).show()

        }
    }


                    fun addUserToDatabase(url:String,imageName:String){
                        val name:String = addBinding.editTextAddName.text.toString()
                        val age: Int = addBinding.editTextAddAge.text.toString().toInt()
                        val email: String = addBinding.editTextAddEmail.text.toString()

                        val id: String = myReference.push().key.toString()
                       val user= (Users(id,name,age,email,url,imageName))
                        myReference.child(id).setValue(user).addOnCompleteListener {task->
                            if (task.isSuccessful){
                                Toast.makeText(applicationContext,"The new user is added to the database",Toast.LENGTH_SHORT).show()
                                addBinding.buttonAddUser.isClickable = true
                                addBinding.progressBarAddUser.visibility = View.INVISIBLE
                                finish()
                            }
                            else{
                                Toast.makeText(applicationContext,task.exception.toString(),Toast.LENGTH_SHORT).show()
                            }
                        }

                    }

    fun uploadPhoto() {

        addBinding.buttonAddUser.isClickable = false
        addBinding.progressBarAddUser.visibility = View.VISIBLE

        //UUID

        val imageName = UUID.randomUUID().toString()

        val imageReference = storageReference.child("images").child(imageName)

        imageUri?.let { uri ->

            imageReference.putFile(uri).addOnSuccessListener {

                Toast.makeText(applicationContext, "Image uploaded", Toast.LENGTH_SHORT).show()

                //downloadable url

                val myUploadedImageReference = storageReference.child("images").child(imageName)

                myUploadedImageReference.downloadUrl.addOnSuccessListener { url ->

                    val imageURL = url.toString()

                    addUserToDatabase(imageURL, imageName)

                }

            }.addOnFailureListener {

                Toast.makeText(applicationContext, it.localizedMessage, Toast.LENGTH_SHORT).show()

            }

        }
    }


}