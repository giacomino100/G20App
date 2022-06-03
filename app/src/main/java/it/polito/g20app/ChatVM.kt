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
    var messaggi: List<Map<*,*>>,
    val idTimeSlot: String, //campo per selezionare se un messaggio deve andare a destra quindi tra i messaggi inviati o tra quuelli ricevuti
    val receiver: String
    )

class ChatVM: ViewModel() {
    private val _messages1 = MutableLiveData<List<Chat>>()

    val messages1 : LiveData<List<Chat>> = _messages1

    private var l1: ListenerRegistration

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var auth: FirebaseAuth = Firebase.auth

    init {
        //qua occorre selezionare la chat con id receiver e sender corretti
        l1 = db.collection("chats")
            .whereEqualTo("sender", auth.uid.toString()) //OK
            .addSnapshotListener { v, e ->
                if(e==null){
                    _messages1.value = v!!.mapNotNull { d ->
                        d.toChat()
                    }
                }
                else {
                    _messages1.value = emptyList()
                }
            }
        }

    fun addChat(newChat: Chat){
        val newChat = hashMapOf(
            "sender" to newChat.sender,
            "messaggi" to newChat.messaggi,
            "receiver" to newChat.receiver,
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
            val messaggi = get("messaggi") as List<Map<*,*>>
            val sender = get("sender") as String
            val idTimeSlot = get("idTimeSlot") as String
            val receiver = get("receiver") as String
            Chat(sender, messaggi, idTimeSlot, receiver)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }



}