package it.polito.g20app

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class ChatFragment : Fragment() {

    private val viewModelC by viewModels<ChatVM>()
    private var idTimeSlot: String = " "
    private var idVendor: String = " "
    private var idChat: String = " "
    private var fromSkillDet: Int = 0
    private var auth: FirebaseAuth = Firebase.auth
    private val viewModelT by viewModels<TimeSlotVM>()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments.let {
            if (it != null) {
                idTimeSlot = it.getString("idTimeSlot") as String
                idVendor = it.getString("idVendor") as String
                fromSkillDet = it.getInt("fromSkillDet")
                if(fromSkillDet == 0) {
                    idChat = it.getString("idChat") as String
                    Log.d("chat_init1",idChat)
                }
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

        viewModelC.chats.observe(viewLifecycleOwner){
            if (fromSkillDet == 1) {
                //Coming from skillDet, I'm a buyer
                //So we have to filter chats containing auth.uid as idBuyer
                if (it.any { c -> c.idTimeSlot == idTimeSlot && c.idBuyer == auth.uid }) {
                    //if there a chat for auth.uid as buyer, for the clicked timeslot ->
                    //Loading the chat (if exists)
                    val myChat = it.filter { c -> c.idTimeSlot == idTimeSlot && c.idBuyer == auth.uid }[0]
                    val myListOfMessage = mutableListOf<Message>()
                    myChat.messages.map { item ->
                        val messages = item.values.toMutableList()
                        myListOfMessage.add(Message(messages[1].toString(), messages[0].toString()))
                    }
                    val adapter = MessageAdapter(myListOfMessage, auth.uid.toString(), idVendor)
                    rv.adapter = adapter
                } else {
                    //Creating a new chat
                    val newChat = Chat("", auth.uid.toString(), emptyList(), idTimeSlot, idVendor)
                    idChat = viewModelC.addChat(newChat)
                    db.collection("chats").get().addOnSuccessListener { it3 ->
                        idChat = it3.filter { it2->
                            it2.data.get("idBuyer") == newChat.idBuyer && it2.data.get("idVendor") == newChat.idVendor && it2.data.get("idTimeSlot") == newChat.idTimeSlot
                        }.map { it1 -> it1.id }.get(0)
                    }
                }
                    Log.d("chats","$idTimeSlot + $idVendor" )
                    Log.d("chats", viewModelC.chats.value!!.filter { it1 -> it1.idTimeSlot == idTimeSlot && it1.idBuyer == auth.uid && it1.idVendor == idVendor }.toString())
                    Log.d("chat_init",idChat)

            } else {
                //Coming from timeSlots list
                //So we have to filter chats containing auth.uid as idVendor
                val myChat = it.filter { c -> c.id == arguments.let { b -> b!!.getString("idChat") } }[0]
                val myListOfMessage = mutableListOf<Message>()
                myChat.messages.map { item ->
                    val messages = item.values.toMutableList()
                    myListOfMessage.add(Message(messages[1].toString(), messages[0].toString()))
                }
                val adapter = MessageAdapter(myListOfMessage, myChat.idVendor, myChat.idBuyer)
                rv.adapter = adapter
            }

        }

        accept.setOnClickListener {
            //Clicking the accept button, the timeslot 'taken' property will be updated (with the value true) on the db
            //TODO: check sul funzionamento
            viewModelT.timeSlots.observe(viewLifecycleOwner) {
                val ts = it.filter { t -> t.id == idTimeSlot }[0]
                ts.taken = true
                viewModelT.updateTimeSlot(ts) //update del db, forse non conviene farlo qui, bisognerebbe controllare
                //TODO: perchè non si può fare qui l'update?
            }
        }

        reject.setOnClickListener {
            //Rejecting buyer request: deleting the chat for the specified timeslot
            //TODO: se si clicca sul reject si chiude la chat e si manda un messaggio automatico al requestor
            val bundle = Bundle()
            bundle.putString("idTimeSlot", idTimeSlot)
            bundle.putString("idVendor", idVendor)
            bundle.putInt("isSkillDet", fromSkillDet)
            Log.d("deleteChat", viewModelC.chats.value!!.filter { c -> c.id  == arguments.let { b -> b!!.getString("idChat") }}.toString())
            viewModelC.deleteChat(viewModelC.chats.value!!.filter { c -> c.id  == arguments.let { b -> b!!.getString("idChat") }}.map { it.id }.toString())
            findNavController().navigate(R.id.action_nav_chatFragment_to_nav_timeslot_chats_fragment, bundle)
        }


         root.findViewById<ImageView>(R.id.button_send).setOnClickListener {
             //creating a new message
             val newMessage = mapOf(
                 "idUser" to auth.uid.toString(),
                 "text" to root.findViewById<EditText>(R.id.messageBox).text.toString()
             )

             //updating the chat
             val myChat = viewModelC.chats.value?.filter { c -> c.id == idChat }?.get(0)
             Log.d("chat",myChat.toString())

             //adding the new message
             val oldMessage = myChat?.messages as MutableList<Map<*,*>>
             oldMessage.add(newMessage)

             //updating the messages vector of the chat
             val newChat = Chat(myChat.id, myChat.idBuyer, oldMessage, myChat.idTimeSlot, myChat.idVendor)
             viewModelC.addMessage(newChat)

             //cleaning the edit field
             root.findViewById<EditText>(R.id.messageBox).text.clear()
        }
        return root
    }

}