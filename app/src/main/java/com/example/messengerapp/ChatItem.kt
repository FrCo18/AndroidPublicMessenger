package com.example.messengerapp

import android.view.View
import android.widget.ImageView

data class ChatItem(var imageView: Int? = null, var title: String? = null, var message: String? = null,var visibilityContext: Int? = View.GONE)
