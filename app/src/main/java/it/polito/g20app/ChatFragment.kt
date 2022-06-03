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

    private val vm by viewModels<MessageVM>()
    private var auth: FirebaseAuth = Firebase.auth
    private var idTimeSlot: String = " "

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments.let {
            if (it != null) {
                idTimeSlot = it.getString("idTimeSlot") as String
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

        vm.messages1.observe(viewLifecycleOwner){
            if(!it.isEmpty()){
                val myChat = it.filter { item -> item.idTimeSlot == idTimeSlot }
                val myListOfMessage = mutableListOf<Message>()
                myChat[0].messaggi.mapNotNull {
                    val valori =  it.values.toMutableList()
                    myListOfMessage.add(Message(valori[0].toString(), valori[1].toString()))
                }
                val adapter = MessageAdapter(myListOfMessage)
                rv.adapter = adapter
            }
        }

        return root
    }

}