package com.example.messengerapp

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity() {
    private var db: DatabaseReference? = null
    private var user: FirebaseUser? = null
    private var mAuth: FirebaseAuth? = null
    private var tvName: TextView? = null
    private var tvSecName: TextView? = null
    private var tvLocation: TextView? = null
    private var tvEmail: TextView? = null
    private var tvStatus: TextView? = null
    private val dbHelper = DbHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        initFields()
        getDataFromDb()
    }

    private fun initFields() {
        tvName = findViewById(R.id.tvNameProfile)
        tvSecName = findViewById(R.id.tvSecNameProfile)
        tvLocation = findViewById(R.id.tvLocationProfile)
        tvEmail = findViewById(R.id.tvEmailProfile)
        tvStatus = findViewById(R.id.tvStatusProfile)
        db = FirebaseDatabase.getInstance().getReference("UserProfile")
        mAuth = FirebaseAuth.getInstance()
        user = mAuth!!.currentUser
    }

    private fun getDataFromDb() {
        val obj = object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds: DataSnapshot in snapshot.children) {
                    val userInfo = ds.getValue(UserInfo::class.java)
                    if (userInfo!!.email == intent.extras!!.get("email")) {
                        tvEmail!!.setText("Email\n" + intent.extras!!.get("email").toString())
                        tvName!!.setText("Имя\n" + userInfo.name)
                        tvLocation!!.setText("Местонахождение\n" + userInfo.location)
                        tvSecName!!.setText("Фамилия\n" + userInfo.secName)

                        if (userInfo.email == user!!.email.toString()) {
                            tvStatus!!.setText("Статус\n" + "online")
                        } else {
                            tvStatus!!.setText("Статус\n" + userInfo.status)
                        }

                        break
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }

        db!!.addValueEventListener(obj)
    }

    fun onClickBackFromProfile(view: View) {
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
    }
}