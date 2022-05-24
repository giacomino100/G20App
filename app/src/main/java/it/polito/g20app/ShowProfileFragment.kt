package it.polito.g20app

import android.app.ProgressDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.File.*

private var currentPhotoPath: String? = null

class ShowProfileFragment : Fragment(R.layout.fragment_home) {

    private val db = Firebase.firestore
    private lateinit var auth: FirebaseAuth
    val viewModel by viewModels<SkillVM>()
    val viewModel2 by viewModels<ProfileVM>()
    private var storageReference: StorageReference = FirebaseStorage.getInstance().getReference("images/")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        // [START initialize_auth]
        // Initialize Firebase Auth
        auth = Firebase.auth
        // [END initialize_auth]
        Log.d("backbutton", "the user is: ${auth.currentUser?.displayName}")

        val rv = root.findViewById<RecyclerView>(R.id.rv_skill_profile)
        rv.layoutManager = LinearLayoutManager(root.context)

        Log.d("backbutton", "carico on create view ")
        val tv1: TextView = root.findViewById(R.id.fullname)
        val tv2: TextView = root.findViewById(R.id.nickname)
        val tv3: TextView = root.findViewById(R.id.email)
        val tv4: TextView = root.findViewById(R.id.location)
        //val tv5: TextView = root.findViewById(R.id.skill1)
        //val tv6: TextView = root.findViewById(R.id.skill2)
        //val tv7: TextView = root.findViewById(R.id.description1)
        //val tv8: TextView = root.findViewById(R.id.description2)


        val img: ImageView = root.findViewById(R.id.imageView_show)
        //val progressDialog = ProgressDialog(this.requireContext())
        //progressDialog.setMessage("Loading image...")
        //progressDialog.setCancelable(false)
        //progressDialog.show()
        var ref = storageReference.child("images/${auth.uid}").downloadUrl.addOnSuccessListener {
            val localFile = File.createTempFile("tempImage", "jpg")
            storageReference.child("images/${auth.uid}").getFile(localFile).addOnSuccessListener {
                //if(progressDialog.isShowing) progressDialog.dismiss()
                var bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                bitmap = Bitmap.createScaledBitmap(bitmap, (bitmap.width*2.5).toInt(), (bitmap.height*2.5).toInt(), false)
                img.setImageBitmap(bitmap)
            }
        }

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
                        tv1.text = document.data!!["fullname"].toString()
                        tv2.text = document.data!!["nickname"].toString()
                        tv3.text = document.data!!["email"].toString()
                        tv4.text = document.data!!["location"].toString()
                    } else {
                        Log.d("backbutton", "Document doesn't exist.")
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
            uSkills.let {
                Log.d("lista di skill",it.toString())
                val adapter = SkillProfileAdapter(it as MutableList<Skill>)
                rv.adapter = adapter
            }


           /* Log.d("skill",uSkills.toString())
            if (uSkills.isNotEmpty() && uSkills.size == 1) {
                uSkills.let {
                    tv5.text = it[0].name
                    tv7.text = it[0].description

                }
            }
                if (uSkills.isNotEmpty() && uSkills.size > 1) {
                    uSkills.let {
                        tv5.text = it[0].name
                        tv6.text = it[1].name
                        tv7.text = it[0].description
                        tv8.text = it[1].description
                    }
            }*/
        }
        viewModel2.profiles.observe(viewLifecycleOwner){ it ->
            val currentUser = it.filter { it.id == auth.uid }[0]
            tv1.text = currentUser.fullname
            tv2.text = currentUser.nickname
            tv3.text = currentUser.email
            tv4.text = currentUser.location
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