package com.example.messengerapp

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class DbHelper() {
    fun setStatus(status: String, user: FirebaseUser) {
        val db: DatabaseReference = FirebaseDatabase.getInstance().getReference("UserProfile")
        val obj = object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                var isFindUserInfo = false
                for (ds: DataSnapshot in snapshot.children) {
                    val userInfo = ds.getValue(UserInfo::class.java)

                    if (userInfo!!.email == user.email.toString()) {
                        isFindUserInfo = true
                        ds.ref.setValue(
                            UserInfo(
                                user.email.toString(),
                                userInfo.name,
                                userInfo.secName,
                                userInfo.location,
                                status
                            )
                        )
                    }
                }
                if (!isFindUserInfo) {
                    db.push().setValue(
                        UserInfo(
                            user.email.toString(),
                            "",
                            "",
                            "",
                            "online"
                        )
                    )
                }
            }


            override fun onCancelled(error: DatabaseError) {
            }
        }

        db.addListenerForSingleValueEvent(obj)
    }
}