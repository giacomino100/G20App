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
    private val viewModelS by viewModels<SkillVM>()
    private val viewModelP by viewModels<ProfileVM>()
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
        val tv5: TextView = root.findViewById(R.id.credit)
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
        }.addOnFailureListener {
            if(progressDialog.isShowing) progressDialog.dismiss()
            Log.d("dialogDismiss", "failure")
        }

        viewModelP.profile.observe(viewLifecycleOwner){
            val myProfile = it.filter { p -> p.id == auth.uid }[0]
            tv1.text = myProfile.fullname
            tv2.text = myProfile.nickname
            tv3.text = myProfile.email
            tv4.text = myProfile.location
            tv5.text = myProfile.credit + " Credits"
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                //se nello spinner non ho selezionato alcuna skill
                if (p0?.getItemAtPosition(p2) != "Select the skill") {
                    val newSkill = viewModelS.skills.value?.filter { it.name == p0?.getItemAtPosition(p2)}!![0].id
                    val newSkillProfile = SkillProfile("", newSkill, auth.uid!!)
                    //verifico se la skill selezionata è già presente tra le skill associate al mio profilo
                    if (!viewModelS.skillsProfile.value!!.map { it.idSkill }.contains(newSkill)){
                        //se non è presente, la aggiungo tra le skill associate ad un utente, altrimenti non faccio nulla
                        viewModelS.addSkillProfile(newSkillProfile)
                    } else {
                        Toast.makeText(requireContext(), "Skill already stored for ${auth.currentUser!!.displayName}", Toast.LENGTH_LONG ).show()
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        viewModelS.skillsProfile.observe(viewLifecycleOwner){
            val adapter = SkillProfileAdapter(viewModelS.skills.value?.filter { s -> it.map { sp -> sp.idSkill }.contains(s.id) } as MutableList<Skill>, true)
            rv.adapter = adapter
            val spinnerChoices = mutableListOf("Select the skill")
            //TODO: filter{it-> it.idUser == auth.uid}
            spinnerChoices.addAll(viewModelS.skills.value!!.map { skill -> skill.name })
            spinner.adapter = ArrayAdapter(this.requireContext(), android.R.layout.simple_spinner_item, spinnerChoices)
        }



        //Salvo informazioni profilo aggiornate tramite BackPress
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        @Suppress("NAME_SHADOWING", "NAME_SHADOWING")
                        val root = view!!.rootView
                        val docData = hashMapOf(
                            "fullname" to view?.findViewById<EditText>(R.id.edit_fullname)!!.text.toString(),
                            "nickname" to view?.findViewById<EditText>(R.id.edit_nickname)!!.text.toString(),
                            "email" to view?.findViewById<EditText>(R.id.edit_email)!!.text.toString(),
                            "location" to view?.findViewById<EditText>(R.id.edit_location)!!.text.toString(),
                            "credit" to view?.findViewById<EditText>(R.id.credit)!!.text.toString().split(" ")[0]
                        )

                        val docref = db.collection("profiles").document(auth.uid.toString())
                        docref.get().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                db.collection("profiles").document(auth.uid.toString()).set(docData)
                                    .addOnSuccessListener {
                                        //Management snackbar
                                        Snackbar.make(root, "Profile updated", Snackbar.LENGTH_LONG).show()
                                    }.addOnFailureListener {
                                        Snackbar.make(root, "Profile update failed", Snackbar.LENGTH_LONG).show()
                                    }
                            } else {
                                Log.d("TAG", "Error: ", task.exception)
                            }
                        }
                        if (isEnabled) {
                            isEnabled = false
                            requireActivity().onBackPressed()
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
                        Snackbar.make(root, "Image successfully uploaded", Snackbar.LENGTH_SHORT).show()
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