package it.polito.g20app

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
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
    private var buyerProfile: Profile = Profile("", "", "", "", "", "")
    private var vendorProfile: Profile = Profile("", "", "", "", "", "")
    private var timeSlotCredit = " "

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Getting data from bundle
        arguments.let {
            if (it != null) {
                idTimeSlot = it.getString("idTimeSlot") as String
                idVendor = it.getString("idVendor") as String

                timeSlotCredit = it.getString("credits") as String
                fromSkillDet = it.getInt("fromSkillDet")
                if(fromSkillDet == 0) {
                    idChat = it.getString("idChat") as String
                    buyerProfile.id = it.getString("idBuyer") as String
                    buyerProfile.credit = it.getString("creditBuyer") as String
                    buyerProfile.fullname = it.getString("fullnameBuyer") as String
                    buyerProfile.nickname = it.getString("nicknameBuyer") as String
                    buyerProfile.email = it.getString("emailBuyer") as String
                    buyerProfile.location = it.getString("locationBuyer") as String
                }
            }
        }

        Log.d("timeSlotCredit", timeSlotCredit)

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

        //Disabling toolbar back button
        (activity as FirebaseActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)

        //If the logged user is a buyer, he cannot accept/reject a timeslot transaction
        if (idVendor != auth.uid){
            reject.isVisible = false
            accept.isVisible = false
        }

        viewModelC.chats.observe(viewLifecycleOwner) {
            if (fromSkillDet == 1) {
                //Coming from skillDet, the logged user is a buyer
                //So we have to filter chats containing auth.uid as idBuyer
                if (it.any { c -> c.idTimeSlot == idTimeSlot && c.idBuyer == auth.uid }) {
                    //if there is a chat for auth.uid as buyer, for the clicked timeslot ->
                    //Loading the chat (if exists)
                    (activity as FirebaseActivity).supportActionBar?.title = "Chat"
                    val myChat = it.filter { c -> c.idTimeSlot == idTimeSlot && c.idBuyer == auth.uid }[0]
                    idChat = myChat.id
                    val myListOfMessage = mutableListOf<Message>()
                    //Mapping the chat messages
                    myChat.messages.map { item ->
                        val messages = item.values.toMutableList()
                        if (messages[0] == "refused" && messages[1] == "Request refused by vendor") {
                            root.findViewById<EditText>(R.id.messageBox).isEnabled = false
                            root.findViewById<ConstraintLayout>(R.id.const2).visibility = View.GONE
                        } else if (messages[0] == "accepted" && messages[1] == "Request accepted by vendor") {
                            root.findViewById<EditText>(R.id.messageBox).isEnabled = false
                            root.findViewById<ConstraintLayout>(R.id.const2).visibility = View.GONE
                        }
                        myListOfMessage.add(Message(messages[1].toString(), messages[0].toString()))
                    }

                    val adapter = MessageAdapter(myListOfMessage, auth.uid.toString(), idVendor)
                    rv.adapter = adapter
                } else {
                    //Creating a new chat
                    (activity as FirebaseActivity).supportActionBar?.title = "New chat"
                    val newChat = Chat("", auth.uid.toString(), emptyList(), idTimeSlot, idVendor )
                    viewModelC.addChat(newChat).addOnSuccessListener { it1 ->
                        idChat = it1.id
                        Log.d("database", "New entry successfully added in chats collection")
                    }.addOnFailureListener {
                        Log.d("database", "Error creating a new chat")
                    }
                }
            } else {
                //Coming from timeSlots list
                //So we have to filter chats containing auth.uid as idVendor
                val myChat =
                    it.filter { c -> c.id == arguments.let { b -> b!!.getString("idChat") } }[0]
                val myListOfMessage = mutableListOf<Message>()
                //Mapping the chat messages
                myChat.messages.map { item ->
                    val messages = item.values.toMutableList()
                    myListOfMessage.add(Message(messages[1].toString(), messages[0].toString()))
                }
                val adapter = MessageAdapter(myListOfMessage, myChat.idVendor, myChat.idBuyer)
                rv.adapter = adapter
            }
        }

        accept.setOnClickListener {
            Log.d("buyerProfile",buyerProfile.credit + " - " + vendorProfile.credit )
            //Clicking the accept button, if the buyer has the amount of requested credits, the timeslot 'taken' and 'buyer' properties will be updated (with the value true) on the db
            if (buyerProfile.credit.toInt() >= timeSlotCredit.toInt()){
                viewModelT.timeSlots.observe(viewLifecycleOwner) {
                    //Updating the timeslot
                    val ts = it.filter { t -> t.id == idTimeSlot }[0]
                    ts.buyer = buyerProfile.id
                    ts.taken = true
                    viewModelT.updateTimeSlot(ts)

                    //updating the chat
                    val myChat = viewModelC.chats.value?.filter { c -> c.id == idChat }?.get(0)

                    val refused = mapOf(
                        "idUser" to "accepted",
                        "text" to "Request accepted by vendor"
                    )
                    //adding the new message
                    val oldMessage = myChat?.messages as MutableList<Map<*,*>>
                    oldMessage.add(refused)

                    //updating the messages vector of the chat
                    val newChat = Chat(myChat.id, myChat.idBuyer, oldMessage, myChat.idTimeSlot, myChat.idVendor)
                    viewModelC.addMessage(newChat)

                    db
                        .collection("profiles")
                        .document(auth.uid.toString())
                        .get()
                        .addOnCompleteListener { it1 ->
                            if (it1.isSuccessful){
                                Log.d("autenticazione", auth.uid.toString() + " - " + it1.result.getString(id.toString()).toString())
                                vendorProfile.id = auth.uid.toString()
                                vendorProfile.credit = it1.result.get("credit").toString()
                                vendorProfile.fullname = it1.result.getString("fullname") as String
                                vendorProfile.nickname= it1.result.getString("nickname") as String
                                vendorProfile.email = it1.result.getString("email") as String
                                vendorProfile.location = it1.result.getString("location") as String

                                creditExchange()
                            }
                        }
                    requireActivity().onBackPressed()

                }
            } else {
                Snackbar.make(root, "The buyer hasn't enough credits", Snackbar.LENGTH_LONG).show()
            }

        }

        reject.setOnClickListener {
            //Once a timeslot transaction is rejected by the vendor, a message "Request refused" is sent to the buyer
            //updating the chat
            val myChat = viewModelC.chats.value?.filter { c -> c.id == idChat }?.get(0)

            val refused = mapOf(
                "idUser" to "refused",
                "text" to "Request refused by vendor"
            )
            //adding the new message
            val oldMessage = myChat?.messages as MutableList<Map<*,*>>
            oldMessage.add(refused)

            //updating the messages vector of the chat
            val newChat = Chat(myChat.id, myChat.idBuyer, oldMessage, myChat.idTimeSlot, myChat.idVendor)
            viewModelC.addMessage(newChat)
            requireActivity().onBackPressed()
        }

         root.findViewById<ImageView>(R.id.button_send).setOnClickListener {
             //creating a new message
             val newMessage = mapOf(
                 "idUser" to auth.uid.toString(),
                 "text" to root.findViewById<EditText>(R.id.messageBox).text.toString()
             )

             //updating the chat
             val myChat = viewModelC.chats.value?.filter { c -> c.id == idChat }?.get(0)

             //adding the new message
             val oldMessage = myChat?.messages as MutableList<Map<*,*>>
             oldMessage.add(newMessage)

             //updating the messages vector of the chat
             val newChat = Chat(myChat.id, myChat.idBuyer, oldMessage, myChat.idTimeSlot, myChat.idVendor)
             viewModelC.addMessage(newChat)

             //cleaning the edit field
             root.findViewById<EditText>(R.id.messageBox).text.clear()
        }

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (!root.findViewById<EditText>(R.id.messageBox).isEnabled)
                        //If the messageBox isn't enabled, it means the vendor refuses the timeslot transactions.
                        //So, going back to the timeslot detail fragment, the chat is deleted from the db
                        viewModelC.deleteChat(viewModelC.chats.value!!.filter { c -> c.idBuyer == auth.uid && c.idTimeSlot == idTimeSlot && c.idVendor == idVendor }.map { c -> c.id }[0])
                    if (isEnabled) {
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            }
            )

        return root
    }

    fun creditExchange(){
        Log.d("profiles", buyerProfile.toString() + "\n" + vendorProfile.toString())
        val buyer = hashMapOf(
            "fullname" to buyerProfile.fullname,
            "nickname" to buyerProfile.nickname,
            "email" to buyerProfile.email,
            "location" to buyerProfile.location,
            "credit" to (buyerProfile.credit.toInt() - timeSlotCredit.toInt()).toString()
        )
        val vendor = hashMapOf(
            "fullname" to vendorProfile.fullname,
            "nickname" to vendorProfile.nickname,
            "email" to vendorProfile.email,
            "location" to vendorProfile.location,
            "credit" to (vendorProfile.credit.toInt() + timeSlotCredit.toInt()).toString()
        )

        Log.d("autenticazione2", buyerProfile.id + " - " + vendorProfile.id)
        val buyerRef = db.collection("profiles").document(buyerProfile.id)
        buyerRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                db.collection("profiles").document(buyerProfile.id).set(buyer)
                    .addOnSuccessListener {
                        Log.d("database", "Buyer's credit updated")
                    }.addOnFailureListener {
                        Log.d("database", "Error updating buyer's credit")
                    }
            } else {
                Log.d("TAG", "Error: ", task.exception)
            }
        }
        val vendorRef = db.collection("profiles").document(vendorProfile.id)
        vendorRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                db.collection("profiles").document(auth.uid.toString()).set(vendor)
                    .addOnSuccessListener {
                        Log.d("database", "Vendor's credit updated")
                    }.addOnFailureListener {
                        Log.d("database", "Error updating vendor's credit")
                    }
            } else {
                Log.d("TAG", "Error: ", task.exception)
            }
        }
    }
}