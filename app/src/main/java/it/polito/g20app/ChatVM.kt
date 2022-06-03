package it.polito.g20app

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.ktx.Firebase
import java.util.*

data class Chat(
    val sender: String,
    var messages: List<Map<*,*>>,
    val idTimeSlot: String,
    val receiver: String
    )

class ChatVM: ViewModel() {
    private val _chats = MutableLiveData<List<Chat>>()

    val chats : LiveData<List<Chat>> = _chats

    private var l1: ListenerRegistration

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var auth: FirebaseAuth = Firebase.auth

    init {
        l1 = db.collection("chats")
            .addSnapshotListener { v, e ->
                if(e==null){
                    _chats.value = v!!.mapNotNull { d ->
                        d.toChat()
                    }
                }
                else {
                    _chats.value = emptyList()
                }
            }
        }

    fun addChat(newChat: Chat){
        val newChat = hashMapOf(
            "idBuyer" to newChat.sender,
            "messages" to newChat.messages,
            "idVendor" to newChat.receiver,
            "idTimeSlot" to newChat.idTimeSlot,
        )
        Log.d("newChat", newChat.toString())

        db.collection("chats").document().set(newChat).addOnSuccessListener {
            Log.d("database", "New entry successfully added in chats collection")
        }.addOnFailureListener {
            Log.d("database", "Error saving a new entry in chats collection")
        }
    }

    private fun DocumentSnapshot.toChat(): Chat? {
        return try {
            val messages = get("messages") as List<Map<*,*>>
            val sender = get("idBuyer") as String
            val idTimeSlot = get("idTimeSlot") as String
            val receiver = get("idVendor") as String
            Chat(sender, messages, idTimeSlot, receiver)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}