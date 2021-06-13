package com.example.messengerapp

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class MainActivity : AppCompatActivity(), RecyclerChatAdapter.OnItemClickListener {
    private var mAuth: FirebaseAuth? = null
    private var db: DatabaseReference? = null
    private var user: FirebaseUser? = null
    private var chatList: ArrayList<ChatItem>? = null
    private var edMessage: EditText? = null
    private var recyclerViewChat: RecyclerView? = null
    private var customAdapter: RecyclerChatAdapter? = null
    private var dbHelper: DbHelper = DbHelper()

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        initFields()
        getDataFromBd()
    }

    fun openTestActivity(view: View) {
        val i = Intent(this, TestActivity::class.java)
        startActivity(i)
    }

    private fun initFields() {
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().getReference("MainChat")
        user = mAuth!!.currentUser
        chatList = ArrayList()
        edMessage = findViewById(R.id.edMessage)
        recyclerViewChat = findViewById(R.id.rvMainChat)
        customAdapter = RecyclerChatAdapter(chatList!!, this)
        recyclerViewChat!!.adapter = customAdapter
        recyclerViewChat!!.layoutManager =
            LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, true)
        recyclerViewChat!!.setHasFixedSize(true)
    }

    private fun getDataFromBd() {
        val obj = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                val listClone: ArrayList<ChatItem> = ArrayList()
                if (chatList!!.size > 0) {
                    chatList!!.clear()
                }

                for (ds: DataSnapshot in snapshot.children) {
                    if (chatList!!.size == 100) {
                        ds.ref.removeValue()
                    } else {
                        val chatItem: ChatItem = ds.getValue(ChatItem::class.java)!!
                        listClone.add(
                            ChatItem(
                                R.drawable.profile_icon,
                                chatItem.title.toString(),
                                chatItem.message.toString()
                            )
                        )
                    }

                    var i = listClone.size - 1
                    if (i >= 0) {
                        chatList!!.clear()
                        while (i >= 0) {
                            chatList!!.add(listClone[i])
                            i--
                        }
                    }

                    customAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }

        db!!.addValueEventListener(obj)
        customAdapter!!.notifyDataSetChanged()
    }

    fun onClickSend(view: View) {
        val message = edMessage!!.text.toString()
        if (message != "") {
            val item = ChatItem(
                R.drawable.profile_icon,
                user!!.email.toString(),
                message
            )
            chatList!!.add(item)
            db!!.push().setValue(item)
            getDataFromBd()
            edMessage!!.setText("")
        }
    }

    fun onClickLogout(view: View) {
        mAuth!!.signOut()
        val i = Intent(this, AuthActivity::class.java)
        startActivity(i)
    }

    fun onClickEditProfile(view: View) {
        val i = Intent(this, EditProfileActivity::class.java)
        startActivity(i)
    }

    override fun onItemClick(position: Int) {
        val i = Intent(this, ProfileActivity::class.java)
        i.putExtra("email", chatList!![position].title)
        startActivity(i)
    }

    override fun onStart() {
        super.onStart()
        dbHelper.setStatus("online", user!!)
    }

    override fun onStop() {
        super.onStop()
        dbHelper.setStatus("offline", user!!)
    }
}