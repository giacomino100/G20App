package it.polito.g20app

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
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


         root.findViewById<ImageView>(R.id.button_send).setOnClickListener {
             val newMessage = mapOf(
                 "idUser" to auth.uid.toString(),
                 "text" to root.findViewById<EditText>(R.id.messageBox).text.toString()
             )

             val myChat = vm.chats.value?.filter { item -> item.idTimeSlot == idTimeSlot && item.idVendor == idVendor }?.get(0)

             val oldMessage = myChat?.messages as MutableList<Map<*,*>>
             oldMessage.add(newMessage)

             val newChat = Chat(myChat.id, myChat.sender, oldMessage, myChat.idTimeSlot, myChat.idVendor)
             vm.addMessage(newChat)
             root.findViewById<EditText>(R.id.messageBox).text.clear()
        }


        return root
    }

}