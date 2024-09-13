package com.singh.firebase

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.singh.firebase.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var toolbar: MaterialToolbar

    lateinit var mainBinding: ActivityMainBinding
    var database: FirebaseDatabase = FirebaseDatabase.getInstance()
    var myReference: DatabaseReference = database.reference.child("MyUsers")
    val userList = ArrayList<Users>()
    lateinit var userAdapter: UsersAdapter
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val firebaseStorage: FirebaseStorage =FirebaseStorage.getInstance()
    val storageReference: StorageReference =firebaseStorage.reference
    val imageNameList = ArrayList<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        toolbar = findViewById(R.id.toolbar)

        mainBinding.floatingActionButton.setOnClickListener {
            val intent = Intent(this, AddActivity::class.java)
            startActivity(intent)

        }

        retrieveDataFromDatabase()

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, LEFT or RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                TODO("Not yet implemented")
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val id = userAdapter.getUserId(viewHolder.adapterPosition)
                myReference.child(id).removeValue()
                val imageName = userAdapter.getUserProfileImageName(viewHolder.adapterPosition)
                val imageReference = storageReference.child("images").child(imageName)
                imageReference.delete()
                Toast.makeText(applicationContext, "The user was deleted", Toast.LENGTH_SHORT)
                    .show()

            }


        }).attachToRecyclerView(mainBinding.recyclerView)

        toolbar.inflateMenu(R.menu.menu_delete_all)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.deleteAll -> {
                    showDialogMessage()
                    true
                }
                R.id.signOut -> {
                    auth.signOut()
                    val intent=Intent(this,LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }

        }


    }

        fun retrieveDataFromDatabase(){
        myReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
            for(eachUser in snapshot.children){
                val user = eachUser.getValue(Users::class.java)
                if (user != null){
                    println("user id is ${user.userId}")
                   println("user name is ${user.userName}")
                    println("user age is ${user.userAge}")
                    println("user email is ${user.userEmail}")
                    println("*****************************************************************")

                    userList.add(user)
                }

                userAdapter = UsersAdapter(this@MainActivity,userList)
                mainBinding.recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
                mainBinding.recyclerView.adapter = userAdapter


            }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    fun showDialogMessage(){
        val dialogMessage = AlertDialog.Builder(this)
        dialogMessage.setTitle("Delete All Notes")
        dialogMessage.setMessage("If click yes all users will be deleted, if you want to delete a particular user swipe left or right, if you want to continue then click no")
        dialogMessage.setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
            dialog.cancel()
        })
        dialogMessage.setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->

            myReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(eachUser in snapshot.children){
                        val user = eachUser.getValue(Users::class.java)

                        if (user != null) {

                            imageNameList.add(user.userProfileImageName)

                        }
                }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })





           myReference.removeValue().addOnCompleteListener{
               task->
               if (task.isSuccessful){
                   for(eachImage in imageNameList){
                       val imageReference=storageReference.child("images").child(eachImage)
                       imageReference.delete()
               }

                   userAdapter.notifyDataSetChanged()

                   Toast.makeText(applicationContext,"All users were deleted",Toast.LENGTH_SHORT).show()
               }
           }



        })

        dialogMessage.create().show()


    }
}