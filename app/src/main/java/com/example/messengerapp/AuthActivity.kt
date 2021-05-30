package com.example.messengerapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class AuthActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var email: EditText? = null
    private var password: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        initFields()
    }

    private fun initFields() {
        mAuth = FirebaseAuth.getInstance()
        if (mAuth!!.currentUser != null) {
            if (mAuth!!.currentUser!!.isEmailVerified) {
                startMainActivity()
            }
        }
        password = findViewById(R.id.edPasswordLogin)
        email = findViewById(R.id.edEmailLogin)
    }

    override fun onStart() {
        super.onStart()
        startMainActivity(false)
    }

    private fun startMainActivity(isLoginButton: Boolean = false) {
        val user = mAuth!!.currentUser
        if (user != null) {
            if (user.isEmailVerified) {
                val i = Intent(this, MainActivity::class.java)
                startActivity(i)
            } else {
                if (isLoginButton) {
                    Toast.makeText(
                        applicationContext,
                        "Вы не подтвердили ваш email!",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }
        }
    }

    fun onClickRegister(view: View) {
        val i = Intent(this, RegisterActivity::class.java)
        startActivity(i)
    }

    fun onCLickLogin(view: View) {
        val passwordText = password!!.text.toString()
        val emailText = email!!.text.toString()
        if (emailText != "" && passwordText != "") {
            mAuth!!.signInWithEmailAndPassword(emailText, passwordText).addOnCompleteListener {
                if (it.isSuccessful) {
                    startMainActivity(true)
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Не правильны Email или Password!",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        } else {
            Toast.makeText(applicationContext, "Не все поля заполненны!", Toast.LENGTH_SHORT)
                .show()
        }
    }
}