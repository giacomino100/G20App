package it.polito.g20app

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

data class Message(
    val text : String,
    val role : String
)

class MessageAdapter(val data: MutableList<Message>, val idBuyer: String, val idVendor: String): RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    private var displayData = data.toMutableList()
    private var auth: FirebaseAuth = Firebase.auth

    class MessageViewHolder(v: View): RecyclerView.ViewHolder(v) {
        private var message = v.findViewById<TextView>(R.id.sent_message)

        fun bind(mess: Message, role: String) {
            message.text = mess.text
            role = mess.role
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        if (auth.uid == idBuyer){
            if(role == idBuyer){
                Log.d("checkif", auth.uid + " 1 " + role)
                return MessageViewHolder(LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.sent_item, parent, false))
                } else {
                Log.d("checkif", auth.uid + " 2 " + role)
                    return MessageViewHolder(LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.receiver_item_layout, parent, false))
                }
        } else {
            if(role == idVendor){
                Log.d("checkif", auth.uid + " 3 " + role)
                return MessageViewHolder(LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.sent_item, parent, false))
                } else {
                Log.d("checkif", auth.uid + " 4 " + role)
                    return MessageViewHolder(LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.receiver_item_layout, parent, false))
                }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val item = displayData[position]
        Log.d("chats", item.text)
        holder.bind(item, item.role)
    }

}