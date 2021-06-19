package com.example.messengerapp

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class PrivateChat : AppCompatActivity(), RecyclerChatAdapter.OnItemClickListener {
    private var mAuth: FirebaseAuth? = null
    private var user: FirebaseUser? = null
    private lateinit var dbPrivateChat: DatabaseReference
    private lateinit var dbProfile: DatabaseReference
    private lateinit var chatList: ArrayList<ChatItem>
    private lateinit var edMessage: EditText
    private lateinit var recyclerViewChat: RecyclerView
    private lateinit var customAdapter: RecyclerChatAdapter
    private var dbHelper: DbHelper = DbHelper()

    //exist this chat
    private var existChat = false

    //chat key
    private var chatKey = ""

    private var emailOpponent = ""

    //Nav fields
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_private_chat)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        initFields()
        getProfileData()
    }

    private fun initFields() {
        mAuth = FirebaseAuth.getInstance()
        dbPrivateChat = FirebaseDatabase.getInstance().getReference("PrivateChats")
        dbProfile = FirebaseDatabase.getInstance().getReference("UserProfile")
        user = mAuth!!.currentUser
        chatList = ArrayList()
        edMessage = findViewById(R.id.edMessage)
        recyclerViewChat = findViewById(R.id.rvMainChat)
        customAdapter = RecyclerChatAdapter(chatList, this@PrivateChat)
        recyclerViewChat.adapter = customAdapter
        recyclerViewChat.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        recyclerViewChat.setHasFixedSize(true)

        emailOpponent = intent.extras!!.get("emailOpponent").toString()

        //init nav fields
        drawerLayout = findViewById(R.id.privateChat)
        toggle =
            ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navView = findViewById(R.id.navView)
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.item_main_chat -> {
                    val i = Intent(this, MainActivity::class.java)
                    startActivity(i)
                }
                R.id.item_private_messages -> {
                    val i = Intent(this, PrivateMessages::class.java)
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


    private fun getProfileData() {
        val obj = object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                mainFor@ for (ds: DataSnapshot in snapshot.children) {
                    val userInfo = ds.getValue(UserInfo::class.java)
                    if (userInfo!!.email == user!!.email) {
                        val chatKeyList: ArrayList<String>? = userInfo.chatKeyList
                        if (chatKeyList != null) {
                            for (key: String in chatKeyList) {
                                if (key.contains(user!!.email.toString()) && key.contains(
                                        emailOpponent
                                    )
                                ) {
                                    chatKey = key
                                    existChat = true
                                    break@mainFor
                                }
                            }
                        }
                        break
                    }
                }
                getMessages()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }

        dbProfile.addListenerForSingleValueEvent(obj)
    }

    @SuppressLint("RestrictedApi")
    private fun getMessages() {
        val obj = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    if (chatList.size > 0) {
                        chatList.clear()
                    }
                    for (ds: DataSnapshot in snapshot.children) {
                        val chatItem: PrivatChatItem =
                            ds.getValue(PrivatChatItem::class.java)!!
                        if (chatKey == chatItem.keyChat) {
                            if (chatItem.chat != null) {
                                val listClone = chatItem.chat!!
                                var i = listClone.size - 1
                                if (i >= 0) {
                                    chatList.clear()
                                    while (i >= 0) {
                                        if (user!!.email.toString() == listClone[i].title.toString()) {
                                            listClone[i].visibilityContext = View.VISIBLE
                                            listClone[i].imageView = R.drawable.profile_icon
                                        }
                                        chatList.add(listClone[i])
                                        i--
                                    }
                                }
                                customAdapter.notifyDataSetChanged()
                            } else {
                                chatList.clear()
                                customAdapter.notifyDataSetChanged()
                            }
                        }
                        break
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }

        val query: Query = dbPrivateChat.orderByChild("keyChat").equalTo(chatKey)
        query.addValueEventListener(obj)

        customAdapter.notifyDataSetChanged()
    }

    private fun addUserChatKey() {
        val obj = object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                mainFor@ for (ds: DataSnapshot in snapshot.children) {
                    val userInfo = ds.getValue(UserInfo::class.java)!!
                    if (userInfo.email == emailOpponent || userInfo.email == user!!.email.toString()) {
                        var chatKeyList: ArrayList<String> = ArrayList()
                        if (userInfo.chatKeyList != null) {
                            chatKeyList = userInfo.chatKeyList!!
                        }

                        existChat = true
                        chatKey = user!!.email.toString() + "+" + emailOpponent
                        chatKeyList.add(chatKey)
                        ds.ref.setValue(
                            UserInfo(
                                userInfo.email,
                                userInfo.name,
                                userInfo.secName,
                                userInfo.location,
                                chatKeyList,
                                userInfo.status
                            )
                        )


                    }
                }

                pushMessage()
                customAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }

        dbProfile.addListenerForSingleValueEvent(obj)
    }

    @SuppressLint("RestrictedApi")
    private fun pushMessage(positionDel: Int? = null) {
        val obj = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                var listClone = ArrayList<ChatItem>()
                if (snapshot.exists()) {
                    for (ds: DataSnapshot in snapshot.children) {

                        val chatItem: PrivatChatItem = ds.getValue(PrivatChatItem::class.java)!!
                        if (chatItem.keyChat == chatKey) {
                            if (chatItem.chat != null) {
                                if (positionDel != null) {
                                    var listCloneDel = chatItem.chat!!
                                    var i = listCloneDel.size - 1
                                    if (i >= 0) {
                                        listClone.clear()
                                        while (i >= 0) {
                                            listClone.add(listCloneDel[i])
                                            i--
                                        }
                                    }
                                    listClone.removeAt(positionDel)
                                    listCloneDel.clear()
                                    listClone.forEach {
                                        listCloneDel.add(ChatItem(
                                            R.drawable.profile_icon,
                                            it.title,
                                            it.message!!.trim()
                                        ))
                                    }
                                    listClone.clear()
                                    i = listCloneDel.size - 1
                                    if (i >= 0) {
                                        listClone.clear()
                                        while (i >= 0) {
                                            listClone.add(listCloneDel[i])
                                            i--
                                        }
                                    }
                                    val k = 0;
                                } else {
                                    listClone = chatItem.chat!!
                                    listClone.add(
                                        ChatItem(
                                            R.drawable.profile_icon,
                                            user!!.email.toString(),
                                            edMessage.text.toString().trim()
                                        )
                                    )
                                }
                            } else {
                                listClone.add(
                                    ChatItem(
                                        R.drawable.profile_icon,
                                        user!!.email.toString(),
                                        edMessage.text.toString().trim()
                                    )
                                )
                            }

                            ds.ref.setValue(PrivatChatItem(chatKey, listClone))
                            break
                        }
                    }
                } else {
                    val message = ChatItem(
                        R.drawable.profile_icon,
                        user!!.email.toString(),
                        edMessage.text.toString().trim()
                    )
                    listClone.add(message)
                    val privateChatItem = PrivatChatItem(chatKey, listClone)
                    dbPrivateChat.push().setValue(privateChatItem)
                }

                edMessage.text.clear()
                getMessages()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }

        val query: Query = dbPrivateChat.orderByChild("keyChat").equalTo(chatKey)
        query.addListenerForSingleValueEvent(obj)

        customAdapter.notifyDataSetChanged()
    }

    fun onClickSend(view: View) {
        if (!existChat) {
            if (edMessage.text.isNotEmpty()) {
                addUserChatKey()
            }
        } else {
            if (edMessage.text.isNotEmpty()) {

                pushMessage()
            }
        }
    }

    override fun onItemClick(position: Int) {
        val i = Intent(this, ProfileActivity::class.java)
        i.putExtra("email", chatList[position].title)
        i.putExtra("backDisplay", "PrivateChat")
        startActivity(i)
    }

    override fun onDeleteClick(position: Int) {
        pushMessage(position)
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