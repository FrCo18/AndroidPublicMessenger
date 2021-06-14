package com.example.messengerapp

import android.annotation.SuppressLint
import android.content.Intent

import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.SearchView

import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager

import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class PrivateMessages : AppCompatActivity(), RecyclerChatAdapter.OnItemClickListener {
    private var mAuth: FirebaseAuth? = null
    private var db: DatabaseReference? = null
    private var dbPrivateChats: DatabaseReference? = null
    private var user: FirebaseUser? = null
    private lateinit var chatList: ArrayList<ChatItem>
    private lateinit var chatListCreated: ArrayList<ChatItem>
    private lateinit var keysChats: ArrayList<String>
    private var customAdapter: RecyclerChatAdapter? = null
    private var customAdapterCreated: RecyclerChatAdapter? = null
    private var recyclerViewChats: RecyclerView? = null
    private var recyclerViewChatsCreated: RecyclerView? = null
    private var dbHelper: DbHelper = DbHelper()

    //Nav fields
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView

    //search
    private lateinit var searchView: SearchView

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_private_messages)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        initFields()
        searchView = findViewById(R.id.searchView)
        getChatsKeys()
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                getUsersFromBd(newText)
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                // task HERE
                return false
            }

        })
    }

    private fun initFields() {
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().getReference("UserProfile")
        dbPrivateChats = FirebaseDatabase.getInstance().getReference("PrivateChats")
        user = mAuth!!.currentUser
        chatList = ArrayList()
        keysChats = ArrayList()
        chatListCreated = ArrayList()

        //Search
        recyclerViewChats = findViewById(R.id.rvPrivateChats)
        customAdapter = RecyclerChatAdapter(chatList, this)
        recyclerViewChats!!.adapter = customAdapter
        recyclerViewChats!!.layoutManager = LinearLayoutManager(this@PrivateMessages)
        recyclerViewChats!!.setHasFixedSize(true)

        //Created
        recyclerViewChatsCreated = findViewById(R.id.rvPrivateChatsCreated)
        customAdapterCreated = RecyclerChatAdapter(chatListCreated, this)
        recyclerViewChatsCreated!!.adapter = customAdapterCreated
        recyclerViewChatsCreated!!.layoutManager = LinearLayoutManager(this@PrivateMessages)
        recyclerViewChatsCreated!!.setHasFixedSize(true)

        //init nav fields
        drawerLayout = findViewById(R.id.private_messages_drawer)
        toggle =
            ActionBarDrawerToggle(this@PrivateMessages, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navView = findViewById(R.id.navViewPrivateMessages)
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.item_main_chat -> {
                    val i = Intent(this, MainActivity::class.java)
                    startActivity(i)
                }
                R.id.item_logout -> {
                    mAuth!!.signOut()
                    val i = Intent(this, AuthActivity::class.java)
                    startActivity(i)
                }
            }
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun getChatsKeys() {
        val obj = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()
                chatListCreated.clear()

                for (ds: DataSnapshot in snapshot.children) {
                    val userInfo: UserInfo = ds.getValue(UserInfo::class.java)!!
                    if (userInfo.email == user!!.email.toString() && userInfo.chatKeyList != null) {
                        keysChats = userInfo.chatKeyList!!
                        break
                    }
                }
                getUsersFromBd()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }
        db!!.addValueEventListener(obj)
    }

    private fun getUsersFromBd(userKeyword: String = "") {
        val obj = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()
                chatListCreated.clear()

                for (ds: DataSnapshot in snapshot.children) {
                    val userInfo: UserInfo = ds.getValue(UserInfo::class.java)!!

                    if (userKeyword.isNotEmpty() && userInfo.email.contains(userKeyword)
                        && userInfo.email != user!!.email.toString()
                    ) {
                        chatList.add(
                            ChatItem(
                                R.drawable.profile_icon,
                                userInfo.email,
                                userInfo.secName + " " + userInfo.name
                            )
                        )
                    } else if (userInfo.email != user!!.email.toString()) {
                        if (userInfo.chatKeyList != null) {
                            keysChats.forEach { keyChat ->
                                userInfo.chatKeyList!!.forEach {
                                    if (keyChat == it) {
                                        chatListCreated.add(
                                            ChatItem(
                                                R.drawable.profile_icon,
                                                userInfo.email,
                                                userInfo.secName + " " + userInfo.name
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    customAdapter!!.notifyDataSetChanged()
                    customAdapterCreated!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }

        if (userKeyword.isEmpty()) {
            recyclerViewChatsCreated!!.visibility = View.VISIBLE
            recyclerViewChats!!.visibility = View.GONE
            db!!.addValueEventListener(obj)
        } else {
            recyclerViewChatsCreated!!.visibility = View.GONE
            recyclerViewChats!!.visibility = View.VISIBLE
            db!!.addListenerForSingleValueEvent(obj)
        }
        customAdapter!!.notifyDataSetChanged()
        customAdapterCreated!!.notifyDataSetChanged()
    }

    override fun onItemClick(position: Int) {
        val i = Intent(this, PrivateChat::class.java)
        if(chatList.size>0){
            i.putExtra("emailOpponent", chatList[position].title)
        }else{
            i.putExtra("emailOpponent", chatListCreated[position].title)
        }

        startActivity(i)
    }

    override fun onDeleteClick(position: Int) {
        TODO("Not yet implemented")
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