package com.example.messengerapp

data class UserInfo(
    var email: String = "",
    var name: String = "",
    var secName: String = "",
    var location: String = "",
    var chatKeyList: ArrayList<String>? = null,
    var status: String = ""
)

//private var emailValue: String = ""
//
//fun setEmail(email: String) {
//    emailValue = email
//}
//fun getEmail(): String
//{
//    return emailValue
//}
//
//fun clearUserInfo(){
//    emailValue = ""
//}
