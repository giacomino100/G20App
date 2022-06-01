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

data class Message(
    val idUser: String,
    val text: String,
    val type: String  //campo per selezionare se un messaggio deve andare a destra quindi tra i messaggi inviati o tra quuelli ricevuti
)

class MessageVM: ViewModel() {
    private val _messages1 = MutableLiveData<List<Message>>()

    val messages1 : LiveData<List<Message>> = _messages1

    private var l1: ListenerRegistration


    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var auth: FirebaseAuth = Firebase.auth

    init {
        l1 = db.collection("chats")
            .addSnapshotListener { v, e ->
                if (e==null) {
                    //LOGICA:
                        // -> si prende il documento dal db che contiente un vettore di mappe di messaggi
                            // -> si prende questo vettore e si crea una lista di messaggi con messageList
                    _messages1.value = v!!.mapNotNull { d ->
                        d.toMessageList()
                    }[0]
            }else
                    _messages1.value = emptyList()
                }
            }

    private fun DocumentSnapshot.toMessageList(): List<Message>? {
        return try {
            //settaggio vettore di messaggi che viene dal DB
            val messaggi = get("messaggi") as List<Map<*,*>>
            val list = mutableListOf<Message>()

            //per ogni mappa (messaggio) si crea un nuovo elemento Message da dare alla recycler view
            messaggi.mapNotNull {
                val valori =  it.values.toMutableList()
                Log.d("chats", valori[0].toString())
                list.add(Message(valori[0].toString(), valori[1].toString(), valori[2].toString()))
            }

            list
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }



}