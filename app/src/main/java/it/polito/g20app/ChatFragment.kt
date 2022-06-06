package it.polito.g20app

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ChatFragment : Fragment() {

    private val vm by viewModels<ChatVM>()
    private var idTimeSlot: String = " "
    private var idVendor: String = " "
    private var auth: FirebaseAuth = Firebase.auth
    private val viewModelT by viewModels<TimeSlotVM>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments.let {
            if (it != null) {
                idTimeSlot = it.getString("idTimeSlot") as String
                idVendor = it.getString("idVendor") as String
            }
        }

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_chat, container, false)
        val rv = root.findViewById<RecyclerView>(R.id.recyclerView_messages)
        rv.layoutManager = LinearLayoutManager(root.context)

        val reject = root.findViewById<Button>(R.id.button2)
        val accept = root.findViewById<Button>(R.id.button3)
        if (idVendor != auth.uid){
            reject.isVisible = false
            accept.isVisible = false
        }

        vm.chats.observe(viewLifecycleOwner){
            if(it.any { item -> item.idTimeSlot == idTimeSlot }){
                //Loading the chat
                val myChat = it.filter { item -> item.idTimeSlot == idTimeSlot && item.idVendor == idVendor }[0]
                val myListOfMessage = mutableListOf<Message>()
                myChat.messages.mapNotNull { item ->
                    val messages = item.values.toMutableList()
                    myListOfMessage.add(Message(messages[1].toString(), messages[0].toString()))
                }
                val adapter = MessageAdapter(myListOfMessage, auth.uid.toString(), idVendor)
                rv.adapter = adapter
            } else {
                //Creating a new chat
                val newChat = Chat("", auth.uid.toString(), emptyList(), idTimeSlot, idVendor)
                vm.addChat(newChat)
            }
        }

        accept.setOnClickListener {
            //se accetto la richiesta per il timeslot, il timeslot a cui appartiene la chat metterà taken = true
            //TODO: check sul funzionamento
            viewModelT.timeSlots.observe(viewLifecycleOwner) {
                val ts = it.filter { t -> t.id == idTimeSlot }[0]
                ts.taken = true
                viewModelT.updateTimeSlot(ts) //update del db, forse non conviene farlo qui, bisognerebbe controllare
            }
        }

        reject.setOnClickListener {
            //TODO: se si clicca sul reject si chiude la chat
        }


         root.findViewById<ImageView>(R.id.button_send).setOnClickListener {
             //creazione nuovo messaggio
             val newMessage = mapOf(
                 "idUser" to auth.uid.toString(),
                 "text" to root.findViewById<EditText>(R.id.messageBox).text.toString()
             )

             //mychat da aggiornare
             val myChat = vm.chats.value?.filter { item -> item.idTimeSlot == idTimeSlot && item.idVendor == idVendor }?.get(0)

             //aggiunta del nuovo messaggio
             val oldMessage = myChat?.messages as MutableList<Map<*,*>>
             oldMessage.add(newMessage)

             //aggiornamento della chat con il vettore dei messaggi aggiornato
             val newChat = Chat(myChat.id, myChat.idBuyer, oldMessage, myChat.idTimeSlot, myChat.idVendor)
             vm.addMessage(newChat)


             //pulizia campo edit
             root.findViewById<EditText>(R.id.messageBox).text.clear()
        }


        return root
    }

}