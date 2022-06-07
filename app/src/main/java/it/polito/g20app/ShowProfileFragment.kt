@file:Suppress("DEPRECATION")

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

private var currentPhotoPath: String? = null

class ShowProfileFragment : Fragment(R.layout.fragment_home) {

    private val db = Firebase.firestore
    private lateinit var auth: FirebaseAuth
    private val viewModelS by viewModels<SkillVM>()
    private val viewModelP by viewModels<ProfileVM>()
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

        val rv = root.findViewById<RecyclerView>(R.id.rv_skill_profile)
        rv.layoutManager = LinearLayoutManager(root.context)

        val tv1: TextView = root.findViewById(R.id.fullname)
        val tv2: TextView = root.findViewById(R.id.nickname)
        val tv3: TextView = root.findViewById(R.id.email)
        val tv4: TextView = root.findViewById(R.id.location)


        val img: ImageView = root.findViewById(R.id.imageView_show)
        val progressDialog = ProgressDialog(this.requireContext())
        progressDialog.setMessage("Loading image...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        storageReference.child("images/${auth.uid}").downloadUrl.addOnSuccessListener {
            val localFile = File.createTempFile("tempImage", "jpg")
            storageReference.child("images/${auth.uid}").getFile(localFile).addOnSuccessListener {
                if(progressDialog.isShowing) progressDialog.dismiss()
                var bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                bitmap = Bitmap.createScaledBitmap(bitmap, (bitmap.width*2.5).toInt(), (bitmap.height*2.5).toInt(), false)
                img.setImageBitmap(bitmap)
            }
        }.addOnFailureListener {
            if(progressDialog.isShowing) progressDialog.dismiss()
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

        viewModelS.skillsProfile.observe(viewLifecycleOwner){
            val adapter = SkillProfileAdapter(viewModelS.skills.value?.filter { s -> it.map { sp -> sp.idSkill }.contains(s.id) } as MutableList<Skill>, false)
            rv.adapter = adapter
        }
        viewModelP.profile.observe(viewLifecycleOwner){
            tv1.text = it.fullname
            tv2.text = it.nickname
            tv3.text = it.email
            tv4.text = it.location
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
                val bundle = Bundle()

                bundle.putString("idUser", auth.uid.toString())
                bundle.putString("path", currentPhotoPath)

                findNavController().navigate(R.id.action_nav_show_profile_to_nav_edit_profile, bundle)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}