package it.polito.g20app

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.ktx.Firebase

data class Chat(
    val id: String,
    val idBuyer: String,
    var messages: List<Map<*,*>>,
    val idTimeSlot: String,
    val idVendor: String
    )

@Suppress("NAME_SHADOWING", "UNCHECKED_CAST")
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
                    }.filter { c -> c.idBuyer == auth.uid || c.idVendor == auth.uid }
                }
                else {
                    _chats.value = emptyList()
                }
            }
        }

    fun addChat(newChat: Chat) : Task<DocumentReference> {
        val newChat = hashMapOf(
            "idBuyer" to newChat.idBuyer,
            "messages" to newChat.messages,
            "idVendor" to newChat.idVendor,
            "idTimeSlot" to newChat.idTimeSlot,
        )

        return db.collection("chats").add(newChat)
    }

    fun addMessage(updatedChat: Chat){
        val newChat = hashMapOf(
            "idBuyer" to updatedChat.idBuyer,
            "messages" to updatedChat.messages,
            "idVendor" to updatedChat.idVendor,
            "idTimeSlot" to updatedChat.idTimeSlot,
        )
        db.collection("chats").document(updatedChat.id).set(newChat).addOnSuccessListener {
            Log.d("database", "New entry successfully added in chats collection")
        }.addOnFailureListener {
            Log.d("database", "Error saving a new entry in chats collection")
        }
    }

    fun deleteChat(idChat :String){
        //TODO: check delete chat when request is rejected
        db.collection("chats").document(idChat).delete().addOnSuccessListener {
            Log.d("delete", "New entry successfully deleted in chats collection")
        }.addOnFailureListener {
            Log.d("delete", "New entry error while deleting chat in collection")
        }
    }

    private fun DocumentSnapshot.toChat(): Chat? {
        return try {
            val messages = get("messages") as List<Map<*,*>>
            val idBuyer = get("idBuyer") as String
            val idTimeSlot = get("idTimeSlot") as String
            val idVendor = get("idVendor") as String
            Chat(id, idBuyer, messages, idTimeSlot, idVendor)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}