package it.polito.g20app

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.drawToBitmap

private const val REQUEST_IMAGE_CAPTURE = 1
private const val REQUEST_ACTION_PICK = 2

private const val name = "icon"
private var currentPhotoPath: String? = null

@Suppress("DEPRECATION")
class EditProfileActivity : AppCompatActivity() {

    private var photo: Photo = Photo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_edit)
        val editButton: ImageButton = findViewById(R.id.imageButton2)
        val img: ImageView = findViewById(R.id.imageView)
        val name: String? = intent.getStringExtra("Name")
        val nick: String? = intent.getStringExtra("Nickname")
        val email: String? = intent.getStringExtra("email")
        val loc: String? = intent.getStringExtra("location")
        val skill1: String? = intent.getStringExtra("skill1")
        val skill2: String? = intent.getStringExtra("skill2")
        val desc1: String? = intent.getStringExtra("description1")
        val desc2: String? = intent.getStringExtra("description2")
        val ed1: EditText = findViewById(R.id.edit_fullname)
        val ed2: EditText = findViewById(R.id.edit_nickname)
        val ed3: EditText = findViewById(R.id.edit_email)
        val ed4: EditText = findViewById(R.id.edit_location)
        val ed5: EditText = findViewById(R.id.edit_skill1)
        val ed6: EditText = findViewById(R.id.edit_skill2)
        val ed7: EditText = findViewById(R.id.edit_description1)
        val ed8: EditText = findViewById(R.id.edit_description2)

        if (intent.getStringExtra("path") != null){
            currentPhotoPath = intent.getStringExtra("path").toString()
            val bitmap: Bitmap? = photo.loadImageFromStorage(currentPhotoPath, "icon")
            img.setImageBitmap(bitmap)
        }

        ed1.setText(name)
        ed2.setText(nick)
        ed3.setText(email)
        ed4.setText(loc)
        ed5.setText(skill1)
        ed6.setText(skill2)
        ed7.setText(desc1)
        ed8.setText(desc2)

        editButton.setOnClickListener {
            val popupMenu = PopupMenu(this, it)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId){
                    R.id.menu_open_camera -> {
                        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        try {
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                        }catch (e : ActivityNotFoundException){
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
    }



    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val image = findViewById<ImageView>(R.id.imageView)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if(data != null) {
                //Da data, ricevuto dall'activity, recuperiamo l'immagine
                val imageBitmap = data.extras?.get("data") as Bitmap

                // set bitmap of edit view
                image.setImageBitmap(imageBitmap)

                // save image to Internal Storage
                currentPhotoPath = photo.saveToInternalStorage(imageBitmap, this, "imageDir", name)
            }else {
                val i = Intent(this,EditCheck::class.java)
                startActivity(i)
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
        if(requestCode == REQUEST_ACTION_PICK && resultCode == RESULT_OK){
            val imageUri: Uri? = data?.data
            image.setImageURI(imageUri)
            val imageBitmap = image.drawToBitmap()
            currentPhotoPath = photo.saveToInternalStorage(imageBitmap, this, "imageDir", name)
        }
    }

    @SuppressLint("WrongThread")
    override fun onBackPressed() {
        val i = Intent()

        val ed1 = findViewById<EditText>(R.id.edit_fullname)
        val ed2: EditText = findViewById(R.id.edit_nickname)
        val ed3: EditText = findViewById(R.id.edit_email)
        val ed4: EditText = findViewById(R.id.edit_location)
        val ed5: EditText = findViewById(R.id.edit_skill1)
        val ed6: EditText = findViewById(R.id.edit_skill2)
        val ed7: EditText = findViewById(R.id.edit_description1)
        val ed8: EditText = findViewById(R.id.edit_description2)

        i.putExtra("Name", ed1.text.toString())
        i.putExtra("Nickname",ed2.text.toString())
        i.putExtra("email",ed3.text.toString())
        i.putExtra("location",ed4.text.toString())
        i.putExtra("skill1",ed5.text.toString())
        i.putExtra("skill2",ed6.text.toString())
        i.putExtra("description1",ed7.text.toString())
        i.putExtra("description2",ed8.text.toString())

        i.putExtra("path", currentPhotoPath)

        setResult(Activity.RESULT_OK,i)

        super.onBackPressed()

    }




}