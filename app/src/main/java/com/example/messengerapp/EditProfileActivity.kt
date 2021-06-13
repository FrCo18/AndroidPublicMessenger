package com.example.messengerapp

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class EditProfileActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var db: DatabaseReference? = null
    private var user: FirebaseUser? = null
    private var edName: EditText? = null
    private var edSecName: EditText? = null
    private var edLocation: EditText? = null
    private var dbHelper: DbHelper = DbHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        initFields()
        getDataFromBd()
    }

    private fun initFields() {
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().getReference("UserProfile")
        user = mAuth!!.currentUser
        edName = findViewById(R.id.edNameEdit)
        edSecName = findViewById(R.id.edSecNameEdit)
        edLocation = findViewById(R.id.edLocationEdit)
    }

    private fun getDataFromBd() {
        val obj = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds: DataSnapshot in snapshot.children) {
                    val userInfo = ds.getValue(UserInfo::class.java)
                    if (userInfo!!.email == user!!.email.toString()) {
                        edName!!.setText(userInfo.name)
                        edSecName!!.setText(userInfo.secName)
                        edLocation!!.setText(userInfo.location)
                        return
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }

        db!!.addValueEventListener(obj)
    }

    fun onClickBack(view: View) {
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
    }

    fun onClickSaveEdit(view: View) {
        val obj = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var isChangeUserInfo = false

                for (ds: DataSnapshot in snapshot.children) {
                    val userInfo = ds.getValue(UserInfo::class.java)

                    if (userInfo!!.email == user!!.email.toString()) {
                        ds.ref.setValue(
                            UserInfo(
                                user!!.email.toString(),
                                edName!!.text.toString(),
                                edSecName!!.text.toString(),
                                edLocation!!.text.toString(),
                                "online"
                            )
                        )
                        break
                    }
                }
                Toast.makeText(applicationContext, "Изменения сохранены!", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }

        db!!.addListenerForSingleValueEvent(obj)

    }
}