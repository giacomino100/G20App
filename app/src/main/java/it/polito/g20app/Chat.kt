package it.polito.g20app

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(val data: MutableList<Chat>): RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {
    private var displayData = data.toMutableList()


    class ChatViewHolder(v: View): RecyclerView.ViewHolder(v) {
        private val title: TextView = v.findViewById(R.id.chat_title)
        private val card: CardView = v.findViewById(R.id.chat_card)

        fun bind(chat: Chat, action: (v: View)->Unit) {
            title.text = chat.sender
            card.setOnClickListener(action)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val vg = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.chat, parent, false)
        return ChatViewHolder(vg)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val item = displayData[position]

        holder.bind(chat =  item) {
            val bundle = Bundle()
            bundle.putString("idTimeSlot", item.idTimeSlot)
            bundle.putString("idVendor", item.idVendor)
            it.findNavController().navigate(R.id.action_nav_timeslot_chats_fragment_to_nav_chatFragment, bundle)
        }
    }

}