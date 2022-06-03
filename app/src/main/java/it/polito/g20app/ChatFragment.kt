package it.polito.g20app

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ChatFragment : Fragment() {

    private val vm by viewModels<ChatVM>()
    private var idTimeSlot: String = " "
    private var receiver: String = " "
    private var auth: FirebaseAuth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments.let {
            if (it != null) {
                idTimeSlot = it.getString("idTimeSlot") as String
                receiver = it.getString("receiver") as String
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
            if(it.filter { item -> item.idTimeSlot == idTimeSlot }.isNotEmpty()){
                //Loading the chat
                val myChat = it.filter { item -> item.idTimeSlot == idTimeSlot && item.receiver == receiver }
                Log.d("chatfiltered", myChat.toString())
                val myListOfMessage = mutableListOf<Message>()
                myChat[0].messages.mapNotNull { item ->
                    val messages = item.values.toMutableList()
                    Log.d("messages", item.values.toMutableList().toString())
                    myListOfMessage.add(Message(messages[1].toString(), messages[0].toString()))
                }
                Log.d("mymessageList", myListOfMessage.toString())
                val adapter = MessageAdapter(myListOfMessage, auth.uid.toString(), receiver)
                rv.adapter = adapter
            } else {
                //Creating a new chat
                val newChat = Chat(auth.uid.toString(), emptyList(), idTimeSlot, receiver)
                vm.addChat(newChat)
            }
        }

        return root
    }

}