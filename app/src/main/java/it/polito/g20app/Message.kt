package it.polito.g20app

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

var type = ""

data class Message(
    val text : String,
    val type : String
)

class MessageAdapter(val data: MutableList<Message>): RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    private var displayData = data.toMutableList()


    class MessageViewHolder(v: View): RecyclerView.ViewHolder(v) {
        private var message = v.findViewById<TextView>(R.id.sent_message)
        fun bind(mess: Message) {
            message.text = mess.text
            type = mess.type
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val vg: View
        if(type == "1"){
            vg = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.sent_item, parent, false)
        } else {
            vg = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.receiver_item_layout, parent, false)
        }


        return MessageViewHolder(vg)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val item = displayData[position]
        Log.d("chats", item.text)
        holder.bind(item)
    }

}