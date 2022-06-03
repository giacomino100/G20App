package it.polito.g20app

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class TimeSlotChatsFragment : Fragment(R.layout.fragment_timeslot_chats) {

    private val viewModelC by viewModels<ChatVM>()
    private var auth: FirebaseAuth = Firebase.auth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_timeslot_chats, container, false)

        val rv = root.findViewById<RecyclerView>(R.id.rv_chats)

        rv.layoutManager = LinearLayoutManager(root.context)

        viewModelC.chats.observe(viewLifecycleOwner) {
            Log.d("timeslotChatsFragment", it.toString())
            val adapter = ChatAdapter(it as MutableList<Chat>)
            rv.adapter = adapter
        }

        return root
    }


}