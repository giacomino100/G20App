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
    val messaggi: List<Map<*,*>>,
    val idTimeSlot: String  //campo per selezionare se un messaggio deve andare a destra quindi tra i messaggi inviati o tra quuelli ricevuti
)

class MessageVM: ViewModel() {
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
                /*if (e==null) {
                    //LOGICA:
                        // -> si prende il documento dal db che contiente un vettore di mappe di messaggi
                            // -> si prende questo vettore e si crea una lista di messaggi con messageList
                    _messages1.value = v!!.mapNotNull { d ->
                        d.toMessageList()
                    }[0]
            }*/
                if(e==null){
                    _messages1.value = v!!.mapNotNull { d ->
                        d.toChat()
                    }
                }
                else
                    _messages1.value = emptyList()
                }
            }

    private fun DocumentSnapshot.toChat(): Chat? {
        return try {
            //settaggio vettore di messaggi che viene dal DB
           /* val messaggi = get("messaggi") as List<Map<*,*>>
            val list = mutableListOf<Message>()

            //per ogni mappa (messaggio) si crea un nuovo elemento Message da dare alla recycler view
            messaggi.mapNotNull {
                val valori =  it.values.toMutableList()
                Log.d("chats", valori[0].toString())
                list.add(Message(valori[0].toString(), valori[1].toString(), valori[2].toString()))
            }

            list*/
            val messaggi = get("messaggi") as List<Map<*,*>>
            val sender = get("sender") as String
            val receiver = get("idTimeSlot") as String
            Chat(sender, messaggi, receiver)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }



}