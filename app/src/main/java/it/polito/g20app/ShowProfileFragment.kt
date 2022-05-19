package it.polito.g20app

import android.os.Bundle
import android.provider.MediaStore.MediaColumns.DOCUMENT_ID
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

private var currentPhotoPath: String? = null

class ShowProfileFragment : Fragment(R.layout.fragment_home) {
    private var photo: Photo = Photo()
    private val db = Firebase.firestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        val tv1: TextView = root.findViewById(R.id.fullname)
        val tv2: TextView = root.findViewById(R.id.nickname)
        val tv3: TextView = root.findViewById(R.id.email)
        val tv4: TextView = root.findViewById(R.id.location)
        val tv5: TextView = root.findViewById(R.id.skill1)
        val tv6: TextView = root.findViewById(R.id.skill2)
        val tv7: TextView = root.findViewById(R.id.description1)
        val tv8: TextView = root.findViewById(R.id.description2)

        // [START initialize_auth]
        // Initialize Firebase Auth
        auth = Firebase.auth
        // [END initialize_auth]

        val docData = hashMapOf(
            "fullname" to auth.currentUser!!.displayName,
            "nickname" to "",
            "email" to "",
            "location" to "",
            "skill1" to "",
            "skill2" to "",
            "description1" to "",
            "description2" to "",
        )

        val docref = db.collection("profiles").document(auth.uid.toString())
        docref.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if(document != null) {
                    if (document.exists()) {
                        Log.d("TAG", "Document already exists.")
                        tv1.text = document.data!!["fullname"].toString()
                        tv2.text = document.data!!["nickname"].toString()
                        tv3.text = document.data!!["email"].toString()
                        tv4.text = document.data!!["location"].toString()
                        tv5.text = document.data!!["skill1"].toString()
                        tv6.text = document.data!!["skill2"].toString()
                        tv7.text = document.data!!["description1"].toString()
                        tv8.text = document.data!!["description2"].toString()
                    } else {
                        Log.d("TAG", "Document doesn't exist.")
                        db.collection("profiles").document(auth.uid.toString()).set(docData)
                            .addOnSuccessListener {
                                Toast.makeText(this.requireContext(), "Data Saved", Toast.LENGTH_SHORT)
                            }.addOnFailureListener {
                                Toast.makeText(this.requireContext(), "Error", Toast.LENGTH_LONG)
                            }
                    }
                }
            } else {
                Log.d("TAG", "Error: ", task.exception)
            }
        }

        return root

    }


    /* Enable option menu with icon for edit profile */
    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateOptionsMenu(show_profile_menu: Menu, inflater: MenuInflater) {
        //menu item that showing the edit icon
        inflater.inflate(R.menu.show_profile_menu, show_profile_menu)
        super.onCreateOptionsMenu(show_profile_menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.edit_profile_icon -> {
                //Opening edit profile fragment
                val fullname = view?.findViewById<TextView>(R.id.fullname)?.text.toString()
                val nickname = view?.findViewById<TextView>(R.id.nickname)?.text.toString()
                val email = view?.findViewById<TextView>(R.id.email)?.text.toString()
                val location = view?.findViewById<TextView>(R.id.location)?.text.toString()
                val skill1 = view?.findViewById<TextView>(R.id.skill1)?.text.toString()
                val skill2 = view?.findViewById<TextView>(R.id.skill2)?.text.toString()
                val description1 = view?.findViewById<TextView>(R.id.description1)?.text.toString()
                val description2 = view?.findViewById<TextView>(R.id.description2)?.text.toString()

                var bundle = Bundle()

                bundle.putString("uid", auth.uid.toString())
                bundle.putString("full name", fullname)
                bundle.putString("nickname", nickname)
                bundle.putString("email", email)
                bundle.putString("location", location)
                bundle.putString("skill1", skill1)
                bundle.putString("skill2", skill2)
                bundle.putString("description1", description1)
                bundle.putString("description2", description2)
                bundle.putString("path", currentPhotoPath)

                findNavController().navigate(R.id.action_nav_show_profile_to_nav_edit_profile, bundle)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }



}