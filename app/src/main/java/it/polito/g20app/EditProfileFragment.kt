package it.polito.g20app

import android.content.ActivityNotFoundException
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.drawToBitmap
import androidx.fragment.app.Fragment
import org.json.JSONException
import org.json.JSONObject


private const val REQUEST_IMAGE_CAPTURE = 1
private const val REQUEST_ACTION_PICK = 2

private const val name = "icon"
private var currentPhotoPath: String? = null

class EditProfileFragment : Fragment(R.layout.fragment_edit) {

    private var photo: Photo = Photo()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_edit, container, false)

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

        arguments.let {
            if (it != null) {
                tv1.text = (it.get("full name").toString())
                tv2.text = (it.get("nickname").toString())
                tv3.text = (it.get("email").toString())
                tv4.text = (it.get("location").toString())
                tv5.text = (it.get("skill1").toString())
                tv6.text = (it.get("skill2").toString())
                tv7.text = (it.get("description1").toString())
                tv8.text = (it.get("description2").toString())

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

        //setting save button
        val saveButton = root.findViewById<Button>(R.id.save_button)
        tv1.setOnClickListener {
            saveButton.setEnabled(true)
            saveButton.setClickable(true)
        }
        tv2.setOnClickListener {
            saveButton.setEnabled(true)
            saveButton.setClickable(true)
        }
        tv3.setOnClickListener {
            saveButton.setEnabled(true)
            saveButton.setClickable(true)
            Log.d("email","funzia")
        }
        tv4.setOnClickListener {
            saveButton.setEnabled(true)
            saveButton.setClickable(true)
        }
        tv5.setOnClickListener {
            saveButton.setEnabled(true)
            saveButton.setClickable(true)
        }
        tv6.setOnClickListener {
            saveButton.setEnabled(true)
            saveButton.setClickable(true)
        }
        tv7.setOnClickListener {
            saveButton.setEnabled(true)
            saveButton.setClickable(true)
        }
        tv8.setOnClickListener {
            saveButton.setEnabled(true)
            saveButton.setClickable(true)
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


        saveButton.setOnClickListener {
            saveButton.setClickable(false)
            saveButton.setEnabled(false)
            val tvFullName = view?.findViewById<TextView>(R.id.edit_fullname)
            val tvNickname = view?.findViewById<TextView>(R.id.edit_nickname)
            val tvEmail = view?.findViewById<TextView>(R.id.edit_email)
            val tvLocation = view?.findViewById<TextView>(R.id.edit_location)
            val tvSkill1 = view?.findViewById<TextView>(R.id.edit_skill1)
            val tvSkill2 = view?.findViewById<TextView>(R.id.edit_skill2)
            val tvDescr1 = view?.findViewById<TextView>(R.id.edit_description1)
            val tvDescr2 = view?.findViewById<TextView>(R.id.edit_description2)

            val profileInfo = "" +
                    "{'full name' : '${tvFullName?.text}', " +
                    "nickname : '${tvNickname?.text}', " +
                    "email : '${tvEmail?.text}', " +
                    "location : '${tvLocation?.text}', " +
                    "skill1 : '${tvSkill1?.text}'," +
                    "skill2 : '${tvSkill2?.text}', " +
                    "description1 : '${tvDescr1?.text}', " +
                    "description2 : '${tvDescr2?.text}'," +
                    " " + "path: '${currentPhotoPath}'}"
            val json = JSONObject(profileInfo)

            val sharedPref = activity?.getPreferences(MODE_PRIVATE)
            if (sharedPref != null) {
                with(sharedPref.edit()) {
                    putString("profile", json.toString())
                    Log.d("load", json.toString())
                    apply()
                }
            }

        }

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