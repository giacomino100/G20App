package it.polito.g20app

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

class MessageAdapter(val data: MutableList<Message>, private val idBuyer: String, private val idVendor: String): RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    private var displayData = data.toMutableList()
    private var auth: FirebaseAuth = Firebase.auth

    class MessageViewHolder(v: View): RecyclerView.ViewHolder(v) {
        private var messageBlue = v.findViewById<TextView>(R.id.sent_message)
        private var messageWhite = v.findViewById<TextView>(R.id.receiver_message)

        fun bind(mess: Message) {
            messageBlue.text = mess.text
            messageWhite.text = mess.text
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return MessageViewHolder(LayoutInflater
            .from(parent.context)
            .inflate(R.layout.sent_item, parent, false))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val item = displayData[position]

        if (auth.uid == idBuyer) {
            if (item.role == idBuyer) {
                holder.itemView.findViewById<TextView>(R.id.sent_message).apply {
                    visibility = View.VISIBLE
                }
            } else {
                holder.itemView.findViewById<TextView>(R.id.receiver_message).apply {
                    visibility = View.VISIBLE
                }
            }
        } else {
            if (item.role == idVendor){
                holder.itemView.findViewById<TextView>(R.id.receiver_message).apply {
                    visibility = View.VISIBLE
                }
            } else {
                holder.itemView.findViewById<TextView>(R.id.sent_message).apply {
                    visibility = View.VISIBLE
                }
            }
        }
        holder.bind(item)
    }

}