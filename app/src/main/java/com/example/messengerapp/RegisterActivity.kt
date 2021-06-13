package com.example.messengerapp

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var email: EditText? = null
    private var password: EditText? = null
    private var repeatPassword: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        initFields()
    }

    private fun initFields() {
        mAuth = FirebaseAuth.getInstance()
        email = findViewById(R.id.edRegEmail)
        password = findViewById(R.id.edRegPassword)
        repeatPassword = findViewById(R.id.edRegRepeatPassword)
    }

    private fun checkExistEmail(email: String) {

    }

    fun onClickCreateAccount(view: View) {
        val passwordText = password!!.text.toString()
        val repeatPasswordText = repeatPassword!!.text.toString()
        val emailText = email!!.text.toString()
        if (emailText != "" && passwordText != "" && repeatPasswordText != "") {

            if (Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                checkExistEmail(emailText)
                if (passwordText == repeatPasswordText) {

                    mAuth!!.createUserWithEmailAndPassword(emailText, passwordText)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                val user = mAuth!!.currentUser

                                user!!.sendEmailVerification().addOnCompleteListener { checkSend ->
                                    if (checkSend.isSuccessful) {
                                        Toast.makeText(
                                            applicationContext,
                                            "Вам на почту выслано сообщение для подтвержденния!",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                        val i = Intent(this, AuthActivity::class.java)
                                        startActivity(i)
                                    } else {
                                        Toast.makeText(
                                            applicationContext,
                                            "Ошибка при отправке сообщения с верификацией!",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()

                                        Toast.makeText(
                                            applicationContext,
                                            "Попроуйте зарегистрироваться ещё раз!",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                        user.delete()
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    applicationContext,
                                    "Ошибка при регистрации!",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                Toast.makeText(
                                    applicationContext,
                                    "Возможно такая почта существует!!",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        }
                } else {
                    Toast.makeText(applicationContext, "Пароли не совпадают!", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(applicationContext, "Почта не валидна!", Toast.LENGTH_SHORT)
                    .show()
            }

        } else {
            Toast.makeText(applicationContext, "Не все поля заполненны!", Toast.LENGTH_SHORT)
                .show()
        }

    }

    fun onClickBack(view: View) {
        val i = Intent(this, AuthActivity::class.java)
        startActivity(i)
    }
}