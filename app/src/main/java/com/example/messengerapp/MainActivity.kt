package com.example.messengerapp

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class MainActivity : AppCompatActivity(), RecyclerChatAdapter.OnItemClickListener {
    private var mAuth: FirebaseAuth? = null
    private var db: DatabaseReference? = null
    private var user: FirebaseUser? = null
    private var chatList: ArrayList<ChatItem>? = null
    private var keysList: ArrayList<String>? = null
    private var edMessage: EditText? = null
    private var recyclerViewChat: RecyclerView? = null
    private var customAdapter: RecyclerChatAdapter? = null
    private var dbHelper: DbHelper = DbHelper()

    //Nav fields
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        initFields()
        getDataFromBd()
    }

    fun openTestActivity(view: View) {
        val i = Intent(this, PrivateMessages::class.java)
        startActivity(i)
    }

    private fun initFields() {
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().getReference("MainChat")
        user = mAuth!!.currentUser
        chatList = ArrayList()
        keysList = ArrayList()
        edMessage = findViewById(R.id.edMessage)
        recyclerViewChat = findViewById(R.id.rvMainChat)
        customAdapter = RecyclerChatAdapter(chatList!!, this)
        recyclerViewChat!!.adapter = customAdapter
        recyclerViewChat!!.layoutManager =
            LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, true)
        recyclerViewChat!!.setHasFixedSize(true)

        //init nav fields
        drawerLayout = findViewById(R.id.menuChatMain)
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navView = findViewById(R.id.navView)
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.item_private_messages -> {
                    val i = Intent(this, PrivateMessages::class.java)
                    startActivity(i)
                }
                R.id.item_logout -> {
                    Toast.makeText(applicationContext, "Clicked item 1", Toast.LENGTH_SHORT).show()
                }
            }
            true
        }
    }

    //for left nav
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun getDataFromBd() {
        val obj = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                val listClone: ArrayList<ChatItem> = ArrayList()
                val keysClone: ArrayList<String> = ArrayList()
                if (chatList!!.size > 0) {
                    chatList!!.clear()
                    keysList!!.clear()
                }

                for (ds: DataSnapshot in snapshot.children) {
                    if (chatList!!.size == 100) {
                        ds.ref.removeValue()
                    } else {
                        val chatItem: ChatItem = ds.getValue(ChatItem::class.java)!!
                        var visibilityContext = View.GONE
                        if (user!!.email.toString() == chatItem.title.toString()) {
                            visibilityContext = View.VISIBLE
                        }
                        listClone.add(
                            ChatItem(
                                R.drawable.profile_icon,
                                chatItem.title.toString(),
                                chatItem.message.toString(),
                                visibilityContext
                            )
                        )
                        keysClone.add(ds.key!!)
                    }

                    var i = listClone.size - 1
                    if (i >= 0) {
                        chatList!!.clear()
                        keysList!!.clear()
                        while (i >= 0) {
                            chatList!!.add(listClone[i])
                            keysList!!.add(keysClone[i])
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
                message.trim()
            )
            chatList!!.add(item)
            db!!.push().setValue(item)
            getDataFromBd()
            edMessage!!.setText("")
        }
    }

    fun onClickEditProfile(view: View) {
        val i = Intent(this, EditProfileActivity::class.java)
        startActivity(i)
    }

    override fun onItemClick(position: Int) {
        val i = Intent(this, ProfileActivity::class.java)
        i.putExtra("email", chatList!![position].title)
        i.putExtra("backDisplay", "MainChat")
        startActivity(i)
    }

    override fun onDeleteClick(position: Int) {
        db!!.child(keysList!![position]).removeValue()

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