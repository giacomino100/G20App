@file:Suppress("DEPRECATION")

package it.polito.g20app

import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.io.File


private const val REQUEST_IMAGE_CAPTURE = 1
private const val REQUEST_ACTION_PICK = 2

@Suppress("DEPRECATION", "DEPRECATION")
class EditProfileFragment : Fragment(R.layout.fragment_edit) {

    private val db = Firebase.firestore
    private var auth: FirebaseAuth = Firebase.auth
    val viewModel by viewModels<SkillVM>()
    val viewModel2 by viewModels<ProfileVM>()
    private var storageReference: StorageReference = FirebaseStorage.getInstance().getReference("images/")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_edit, container, false)

        val rv = root.findViewById<RecyclerView>(R.id.rv_skill_edit_profile)
        rv.layoutManager = LinearLayoutManager(root.context)

        //Questa riga per disattivare il tasto back nella toolbar
        (activity as FirebaseActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)

        val tv1: TextView = root.findViewById(R.id.edit_fullname)
        val tv2: TextView = root.findViewById(R.id.edit_nickname)
        val tv3: TextView = root.findViewById(R.id.edit_email)
        val tv4: TextView = root.findViewById(R.id.edit_location)
        val spinner: Spinner = root.findViewById(R.id.spinnerEditProfile)

        val img: ImageView = root.findViewById(R.id.imageView_edit)

        val progressDialog = ProgressDialog(this.requireContext())
        progressDialog.setMessage("Loading image...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        storageReference.child("images/${auth.uid}").downloadUrl.addOnSuccessListener {
            val localFile = File.createTempFile("tempImage", "jpg")
            storageReference.child("images/${auth.uid}").getFile(localFile).addOnSuccessListener {
                if(progressDialog.isShowing) progressDialog.dismiss()
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                img.setImageBitmap(bitmap)
            }
        }.addOnFailureListener() {
            if(progressDialog.isShowing) progressDialog.dismiss()
            Log.d("dialogDismiss", "failure")
        }

        viewModel2.profile.observe(viewLifecycleOwner){
            tv1.text = it.fullname
            tv2.text = it.nickname
            tv3.text = it.email
            tv4.text = it.location
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                //se nello spinner non ho selezionato alcuna skill
                if (p0?.getItemAtPosition(p2) != "Select the skill") {
                    val newSkill = viewModel.skills.value?.filter { it.name == p0?.getItemAtPosition(p2)}!![0].id
                    val newSkillProfile = hashMapOf(
                        "idUser" to auth.uid.toString(),
                        "idSkill" to newSkill
                    )
                    db.collection("skillsProfile").get().addOnSuccessListener {
                        //verifico se la skill selezionata è già presente tra le skill associate al mio profilo
                        if (it.documents.none { d ->
                                d.data?.get("idUser") == newSkillProfile["idUser"] &&
                                        d.data?.get("idSkill") == newSkillProfile["idSkill"]
                            }) {
                                //se non è presente, la aggiungo tra le skill associate ad un utente, altrimenti non faccio nulla
                            db.collection("skillsProfile").add(newSkillProfile).addOnSuccessListener {
                                Log.d("AddingSkill", "Skill saved for ${auth.currentUser!!.displayName} user")
                            }.addOnFailureListener {
                                Log.d("AddingSkill", "Error saving a new skill for ${auth.currentUser!!.displayName} user")
                            }
                            db.collection("skillsProfile").get()
                                .addOnCompleteListener { task ->
                                    Log.d("readingSkillsProfile", "Reading the skillsProfile")
                                    val skillsP = task.result.documents.filter { d -> d.data?.get("idUser") == auth.uid }.map{ d -> d.get("idSkill") }

                                    Log.d("readingSkillsProfile", viewModel.skills.value?.filter{ skill -> skillsP.contains(skill.id)}.toString())
                                    val adapter = SkillProfileAdapter(viewModel.skills.value?.filter{ skill -> skillsP.contains(skill.id)} as MutableList<Skill>)
                                    rv.adapter = adapter
                                }
                                .addOnFailureListener {
                                    Log.d("readingSkillsProfile", "cannot read the skillsProfile")
                                }
                        }
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        viewModel.skills.observe(viewLifecycleOwner){ it ->
            db.collection("skillsProfile").get()
                .addOnCompleteListener { task ->
                    val skillsP = task.result.documents.filter { it.data?.get("idUser") == auth.uid }.map{ it.get("idSkill") }
                    val adapter = SkillProfileAdapter(it.filter { skill -> skillsP.contains(skill.id)} as MutableList<Skill>)
                    rv.adapter = adapter
                }
                .addOnFailureListener {
                    Log.d("readingSkillsProfile", "cannot read the skillsProfile")
                }
            val spinnerChoices = mutableListOf("Select the skill")
            spinnerChoices.addAll(it.map { skill -> skill.name })
            spinner.adapter = ArrayAdapter(this.requireContext(), android.R.layout.simple_spinner_item, spinnerChoices)
        }

        //Salvo informazioni profilo aggiornate tramite BackPress
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        val root = view!!.rootView
                        val docData = hashMapOf(
                            "fullname" to view?.findViewById<EditText>(R.id.edit_fullname)!!.text.toString(),
                            "nickname" to view?.findViewById<EditText>(R.id.edit_nickname)!!.text.toString(),
                            "email" to view?.findViewById<EditText>(R.id.edit_email)!!.text.toString(),
                            "location" to view?.findViewById<EditText>(R.id.edit_location)!!.text.toString()
                        )

                        val docref = db.collection("profiles").document(auth.uid!!)
                        docref.get().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                db.collection("profiles").document(auth.uid!!).set(docData)
                                    .addOnSuccessListener {
                                        //Management snackbar
                                        Snackbar.make(root, "Profile updated", Snackbar.LENGTH_LONG).show()
                                    }.addOnFailureListener {
                                        Snackbar.make(root, "Profile update failed", Snackbar.LENGTH_LONG).show()
                                    }
                            } else {
                                Log.d("TAG", "Error: ", task.exception)
                            }
                            if (isEnabled) {
                                isEnabled = false
                                requireActivity().onBackPressed()
                            }
                        }
                    }
                }
            )

        //IMPLEMENTAZIONE TASTO PER MODIFICARE LA FOTO DEL PROFILO
        root.findViewById<ImageButton>(R.id.imageButton2)?.setOnClickListener {
                val popupMenu = PopupMenu(this.requireContext(), it)
                popupMenu.setOnMenuItemClickListener { m ->
                    when (m.itemId) {
                        R.id.menu_open_camera -> {
                            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            try {
                                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                            } catch (e: ActivityNotFoundException) {
                                println(e)
                            }
                            true
                        }
                        R.id.menu_open_gallery -> {
                            val galleryIntent = Intent(Intent.ACTION_PICK)
                            galleryIntent.type = "image/*"
                            startActivityForResult(galleryIntent, REQUEST_ACTION_PICK)
                            true
                        }
                        else -> false
                    }
                }
                popupMenu.inflate(R.menu.photo_menu)
                popupMenu.show()
            }

        return root
        }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val image = view?.findViewById<ImageView>(R.id.imageView_edit)

        //Se l'Intent era quello della foto camera dentro questo if
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == AppCompatActivity.RESULT_OK) {
            if(data != null) {
                val imageBitmap = data.extras?.get("data") as Bitmap

                // set bitmap of edit view
                image?.setImageBitmap(imageBitmap)

                //save to firestore
                val ref = storageReference.child("/images/" + auth.uid)
                val baos = ByteArrayOutputStream()
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val img = baos.toByteArray()
                ref.putBytes(img).addOnCompleteListener{
                    if(it.isSuccessful){
                        //Management snackbar
                        val root = requireView().rootView
                        Snackbar.make(root, "Woow, image has been uploaded", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }


        //Se l'intent è quello di caricare la foto dalla galleria dentro questo if
        if(requestCode == REQUEST_ACTION_PICK && resultCode == AppCompatActivity.RESULT_OK){
            val imageUri: Uri? = data?.data
            image?.setImageURI(imageUri)

            //save to firestore
            val ref = storageReference.child("/images/" + auth.uid)
            if (data != null) {
                ref.putFile(data.data!!)
            }
        }
    }

}