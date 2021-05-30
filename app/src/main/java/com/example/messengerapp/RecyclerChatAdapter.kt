package com.example.messengerapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecyclerChatAdapter(
    var list: List<ChatItem>,
    private val listener: OnItemClickListener
) :
    RecyclerView.Adapter<RecyclerChatAdapter.RecyclerViewHolder>() {

    inner class RecyclerViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var imageView: ImageView? = null
        var title: TextView? = null
        var message: TextView? = null

        init {
            imageView = itemView.findViewById(R.id.imChatItem)
            title = itemView.findViewById(R.id.tvName)
            message = itemView.findViewById(R.id.tvMessage)
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false)
        return RecyclerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val item = list[position]
        holder.imageView!!.setImageResource(item.imageView!!)
        holder.title!!.text = item.title!!
        holder.message!!.text = item.message!!
    }

    override fun getItemCount() = list.size
}