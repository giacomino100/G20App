package it.polito.g20app

import android.os.Bundle
import android.provider.MediaStore.MediaColumns.DOCUMENT_ID
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
    val viewModel by viewModels<SkillVM>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        Log.d("backbutton", "carico on create view ")
        val tv1: TextView = root.findViewById(R.id.fullname)
        val tv2: TextView = root.findViewById(R.id.nickname)
        val tv3: TextView = root.findViewById(R.id.email)
        val tv4: TextView = root.findViewById(R.id.location)
        val tv5: TextView = root.findViewById(R.id.skill1)
        val tv6: TextView = root.findViewById(R.id.skill2)
        val tv7: TextView = root.findViewById(R.id.description1)
        val tv8: TextView = root.findViewById(R.id.description2)

        Log.d("savedInstanceState", savedInstanceState.toString())
        // [START initialize_auth]
        // Initialize Firebase Auth
        auth = Firebase.auth
        // [END initialize_auth]
        Log.d("backbutton", "the user is: ${auth.currentUser?.displayName}")



        val docData = hashMapOf(
            "fullname" to auth.currentUser!!.displayName,
            "nickname" to "uberts94",
            "email" to "pietro.ubertini@polito.it",
            "location" to "torino",
        )

        db
            .collection("profiles")
            .document(auth.uid.toString())
            .get()
            .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if(document != null) {
                    if (document.exists()) {
                        Log.d("backbutton", "Document already exists.")
                        //TODO: quando aggiorno un dato e dopo faccio la get non si prendono i dati aggiornati
                        tv1.text = document.data!!["fullname"].toString()
                        tv2.text = document.data!!["nickname"].toString()
                        tv3.text = document.data!!["email"].toString()
                        tv4.text = document.data!!["location"].toString()
                    } else {
                        Log.d("backbutton", "Document doesn't exist.")
                        //quando si fa il login non esiste il documento nel DB. Viene creato quando si va in show profile
                        //creando il documento per Default vengono create due skill, quelle specifiche dell'utente

                        //CREAZIONE NUOVO DOCUMENTO PER UN NUOVO UTENTE
                        db.collection("profiles").document(auth.uid.toString()).set(docData)
                            .addOnSuccessListener {
                                tv1.text = docData.get("fullname").toString()
                                tv2.text = docData.get("nickname").toString()
                                tv3.text = docData.get("email").toString()
                                tv4.text = docData.get("location").toString()
                                //SE IL DOCUMENTO VIENE CREATO CORRETTAMENTE SI CREANO LE DUE SKILL VUOTE
                                val skill1 = hashMapOf(
                                    "idUser" to auth.uid.toString(),
                                    "name" to "skill1",
                                    "description" to "description skill1"
                                )
                                db.collection("skills").document().set(skill1)
                                    .addOnSuccessListener {
                                        val skill2 = hashMapOf(
                                            "idUser" to auth.uid.toString(),
                                            "name" to "skill2",
                                            "description" to "description skill2"
                                        )
                                        db.collection("skills").document().set(skill2)
                                            .addOnSuccessListener {
                                                Toast.makeText(this.requireContext(), "Data Saved", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                            }.addOnFailureListener {
                                Toast.makeText(this.requireContext(), "Error", Toast.LENGTH_LONG).show()
                            }
                    }
                }
            } else {
                Log.d("TAG", "Error: ", task.exception)
            }
        }

        // caricamento skill
        // qui dentro carico dal db le skill di uno user
        // mi memorizzo dentro due varibili l'id delle skill che coincidono con l'id del documento,
        // così all'id del documento nella posizione zero corrisponde il primo campo di text e così per il secondo.
        // Quando vado a salvarli utilizzo questi idskill per recuperare il documento da aggiornare

        viewModel.skills.observe(viewLifecycleOwner){ it ->
            val uSkills = it.filter { it.idUser == auth.uid }

            if (uSkills.isNotEmpty()) {
                uSkills.let {
                    tv5.text = it[0].name
                    tv6.text = it[1].name
                    tv7.text = it[0].description
                    tv8.text = it[1].description
                }
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
                var bundle = Bundle()

                bundle.putString("idUser", auth.uid.toString())
                bundle.putString("path", currentPhotoPath)

                findNavController().navigate(R.id.action_nav_show_profile_to_nav_edit_profile, bundle)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }



}