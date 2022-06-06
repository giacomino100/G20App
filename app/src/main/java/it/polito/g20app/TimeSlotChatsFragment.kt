package it.polito.g20app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class TimeSlotChatsFragment : Fragment(R.layout.fragment_timeslot_chats) {

    private val viewModelC by viewModels<ChatVM>()
    private var auth: FirebaseAuth = Firebase.auth
    private var idTimeSlot = " "

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_timeslot_chats, container, false)
        val rv = root.findViewById<RecyclerView>(R.id.rv_chats)
        (activity as FirebaseActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        rv.layoutManager = LinearLayoutManager(root.context)

        arguments.let {
            idTimeSlot = it!!.getString("idTimeSlot") as String
        }

        viewModelC.chats.observe(viewLifecycleOwner) { it ->
            if (it.none { c -> c.idTimeSlot == arguments.let { b -> b!!.getString("idTimeSlot") } })
                Snackbar.make(root, "No opened chats for the selected timeslot", Snackbar.LENGTH_LONG).show()
            val adapter = ChatAdapter(it.filter { arguments.let { b -> b!!.get("idTimeSlot")
            } == it.idTimeSlot && it.idVendor == auth.uid} as MutableList<Chat>)
            rv.adapter = adapter
        }
        return root
    }
}