package it.polito.g20app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
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
import androidx.core.view.drawToBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.json.JSONException


private const val REQUEST_IMAGE_CAPTURE = 1
private const val REQUEST_ACTION_PICK = 2

private const val name = "icon"
private var currentPhotoPath: String? = null
private val db = Firebase.firestore

class EditProfileFragment : Fragment(R.layout.fragment_edit) {

    private var photo: Photo = Photo()
    private var auth: FirebaseAuth = Firebase.auth
    val viewModel by viewModels<SkillVM>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_edit, container, false)

        //Questa riga per disattivare il tasto back nella toolbar
        (activity as FirebaseActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)

        //set data from bundle
        val tv1: TextView = root.findViewById(R.id.edit_fullname)
        val tv2: TextView = root.findViewById(R.id.edit_nickname)
        val tv3: TextView = root.findViewById(R.id.edit_email)
        val tv4: TextView = root.findViewById(R.id.edit_location)
        val tv5: TextView = root.findViewById(R.id.edit_skill1)
        val tv6: TextView = root.findViewById(R.id.edit_skill2)
        val tv7: TextView = root.findViewById(R.id.edit_description1)
        val tv8: TextView = root.findViewById(R.id.edit_description2)
        val img: ImageView = root.findViewById(R.id.imageView)

        var idSkill1: String = " "
        var idSkill2: String = " "

        var idUser = " "
        arguments.let{
            if (it != null) {
                idUser = it.get("idUser").toString()
                try {
                    if (it.get("path").toString() == "null")
                        currentPhotoPath = null
                    else
                        currentPhotoPath = it.get("path").toString()
                } catch (e: JSONException) {
                    println(e)
                }

                if(currentPhotoPath != null){
                    val bitmap: Bitmap? = photo.loadImageFromStorage(currentPhotoPath, "icon")
                    img.setImageBitmap(bitmap)
                }
            }
        }

        db
            .collection("profiles")
            .document(idUser)
            .get()
            .addOnSuccessListener {
                tv1.text = (it.get("fullname").toString())
                tv2.text = (it.get("nickname").toString())
                tv3.text = (it.get("email").toString())
                tv4.text = (it.get("location").toString())
            }

        // qui dentro carico dal db le skill di uno user
        // mi memorizzo dentro due varibili l'id delle skill che coincidono con l'id del documento,
        // cosi all'id del documento nella posizione zero corrisponde il primo campo di text e cosi per il secondo.
        // QUando vado a salvarli utilizzo questi idskill per recuperare il documento da aggiornare
        viewModel.skills.observe(viewLifecycleOwner){
            lateinit var userSkill: MutableList<Skill>
            it.map { skill ->
                if(skill.idUser == idUser){
                    userSkill.add(skill)
                }
            }
            idSkill1 = userSkill[0].id
            idSkill2 = userSkill[1].id

            tv5.text = userSkill[0].name
            tv6.text = userSkill[1].name
            tv7.text = userSkill[0].description
            tv8.text = userSkill[1].description
        }

        //IMPLEMENTAZIONE TASTO PER MODIFICARE LA FOTO DEL PROFILO
        root.findViewById<ImageButton>(R.id.imageButton2)?.setOnClickListener {
                val popupMenu = PopupMenu(this.requireContext(), it)
                popupMenu.setOnMenuItemClickListener {
                    when (it.itemId) {
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

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    //Updating data on db
                    val docData = hashMapOf(
                        "fullname" to view?.findViewById<EditText>(R.id.edit_fullname)!!.text.toString(),
                        "nickname" to view?.findViewById<EditText>(R.id.edit_nickname)!!.text.toString(),
                        "email" to view?.findViewById<EditText>(R.id.edit_email)!!.text.toString(),
                        "location" to view?.findViewById<EditText>(R.id.edit_location)!!.text.toString()
                    )

                    //Updating db of profile
                    val docref = db.collection("profiles").document(idUser)
                    docref.get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            db.collection("profiles").document(idUser).set(docData)
                                .addOnSuccessListener {
                                }.addOnFailureListener {
                                }
                        } else {
                            Log.d("TAG", "Error: ", task.exception)
                        }
                    }

                    val skill1Updated = hashMapOf(
                        "idUser" to idUser,
                        "name" to view?.findViewById<EditText>(R.id.edit_skill1)!!.text.toString(),
                        "description" to view?.findViewById<EditText>(R.id.edit_description1)!!.text.toString()
                    )
                    db
                        .collection("skills")
                        .document(idSkill1)
                        .set(skill1Updated)
                        .addOnSuccessListener {

                        }
                    val skill2Updated = hashMapOf(
                        "idUser" to idUser,
                        "name" to view?.findViewById<EditText>(R.id.edit_skill2)!!.text.toString(),
                        "description" to view?.findViewById<EditText>(R.id.edit_description2)!!.text.toString()
                    )
                    db
                        .collection("skills")
                        .document(idSkill2)
                        .set(skill2Updated)
                        .addOnSuccessListener {

                        }
                    //Management snackbar
                    val root = view!!.rootView
                    Snackbar.make(root, "Profile updated", Snackbar.LENGTH_LONG).show()

                    if (isEnabled) {
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }


                }
            }
            )
        return root
        }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val image = view?.findViewById<ImageView>(R.id.imageView)

        //Se l'Intent era quello della foto camera dentro questo if
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == AppCompatActivity.RESULT_OK) {
            if(data != null) {
                val imageBitmap = data.extras?.get("data") as Bitmap

                // set bitmap of edit view
                image?.setImageBitmap(imageBitmap)

                // save image to Internal Storage
                currentPhotoPath = photo.saveToInternalStorage(imageBitmap, this.requireActivity(), "imageDir", name)
            }
        }

        //Se l'intent è quello di caricare la foto dalla galleria dentro questo if
        if(requestCode == REQUEST_ACTION_PICK && resultCode == AppCompatActivity.RESULT_OK){
            val imageUri: Uri? = data?.data
            image?.setImageURI(imageUri)
            val imageBitmap = image?.drawToBitmap()
            currentPhotoPath =
                imageBitmap?.let { photo.saveToInternalStorage(it, this.requireActivity(), "imageDir", name) }
        }
    }

}