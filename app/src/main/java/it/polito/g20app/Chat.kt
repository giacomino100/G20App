package it.polito.g20app

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class ChatAdapter(val data: MutableList<Chat>, credit: String): RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {
    private var displayData = data.toMutableList()
    private var credit = credit

    class ChatViewHolder(v: View): RecyclerView.ViewHolder(v) {
        private val title: TextView = v.findViewById(R.id.chat_title)
        private val card: CardView = v.findViewById(R.id.chat_card)
        private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

        @SuppressLint("SetTextI18n")
        fun bind(chat: Chat, action: (v: View) -> Unit) {
            title.text = "Loading..."
            //Getting buyer name from the db: it's used to set the chat card title
            db.collection("profiles").document(chat.idBuyer).get().addOnSuccessListener {
                val fullname = it.get("fullname") as String
                title.text = fullname
                Log.d("database", "Fullname successfully retrieved")
            }.addOnFailureListener {
                Log.d("timeslotChatsFragment", "Error getting the buyer fullname")
            }
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
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()

        holder.bind(chat =  item) {
            val bundle = Bundle()
            bundle.putInt("fromSkillDet", 0)
            bundle.putString("idTimeSlot", item.idTimeSlot)
            bundle.putString("idVendor", item.idVendor)
            bundle.putString("idChat", item.id)
            bundle.putString("credits", credit)

            db
                .collection("profiles")
                .document(item.idBuyer)
                .get()
                .addOnCompleteListener { it1 ->
                    if (it1.isSuccessful){
                        bundle.putString("idBuyer", item.idBuyer)
                        bundle.putString("creditBuyer", it1.result.get("credit").toString() )
                        bundle.putString("fullnameBuyer", it1.result.get("fullname").toString() )
                        bundle.putString("emailBuyer", it1.result.get("email").toString() )
                        bundle.putString("locationBuyer", it1.result.get("location").toString() )
                        bundle.putString("nicknameBuyer", it1.result.get("nickname").toString() )

                        it.findNavController().navigate(R.id.action_nav_timeslot_chats_fragment_to_nav_chatFragment, bundle)
                    }
                }
        }
    }

}