package com.singh.firebase

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.singh.firebase.databinding.UserItemBinding
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception

class UsersAdapter(val context: Context, val userList: ArrayList<Users>): RecyclerView.Adapter<UsersAdapter.UsersViewHolder>()  {

    inner class UsersViewHolder(val adapterBinding: UserItemBinding)  : RecyclerView.ViewHolder(adapterBinding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val binding= UserItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return UsersViewHolder(binding)

    }

    override fun getItemCount(): Int {

        return userList.size

    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        holder.adapterBinding.textViewName.text=userList[position].userName
        holder.adapterBinding.textViewAge.text=userList[position].userAge.toString()
        holder.adapterBinding.textViewEmail.text=userList[position].userEmail

        val imageUrl=userList[position].userProfileImageUrl
        Picasso.get().load(imageUrl).into(holder.adapterBinding.imageView,object:Callback{
            override fun onSuccess() {
                holder.adapterBinding.progressBar.visibility= View.INVISIBLE
            }

            override fun onError(e: Exception?) {
               Toast.makeText(context,e?.localizedMessage,Toast.LENGTH_SHORT).show()
            }
        })

        holder.adapterBinding.linearLayout.setOnClickListener {
            val intent= Intent(context,UpdateActivity::class.java)
            intent.putExtra("userId",userList[position].userId)
            intent.putExtra("userName",userList[position].userName)
            intent.putExtra("userAge",userList[position].userAge)
            intent.putExtra("userEmail",userList[position].userEmail)
            intent.putExtra("userProfileImageUrl",userList[position].userProfileImageUrl)
            intent.putExtra("userProfileImageName",userList[position].userProfileImageName)
            context.startActivity(intent)

        }
    }

    fun getUserId(position: Int): String{
        return userList[position].userId

    }

    fun getUserProfileImageName(position: Int): String{
        return userList[position].userProfileImageName
    }
}