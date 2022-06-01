package it.polito.g20app

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase



class MessageAdapter(val data: MutableList<Message>): RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    private var displayData = data.toMutableList()

    private var RIGHT = "1"

    class MessageViewHolder(v: View): RecyclerView.ViewHolder(v) {
        private var message = v.findViewById<TextView>(R.id.sent_message)

        private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        private var auth: FirebaseAuth = Firebase.auth

        fun bind(mess: String) {
            message.text = mess
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val vg: View
        if(RIGHT == "1"){
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
        holder.bind(item.text)
    }

}