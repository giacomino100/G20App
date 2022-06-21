package it.polito.g20app

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import it.polito.g20app.databinding.ActivityFirebaseBinding
import java.io.File

class FirebaseActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityFirebaseBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private var storageReference: StorageReference = FirebaseStorage.getInstance().getReference("images/")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFirebaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        // [START initialize_auth]
        // Initialize Firebase Auth
        auth = Firebase.auth
        // [END initialize_auth]

        //Menù header initialization
        //Setting Image Profile
        val headerView: View = binding.navView.getHeaderView(0)
        val img: ImageView = headerView.findViewById(R.id.nav_head_avatar)
        storageReference.child("images/${auth.uid}").downloadUrl.addOnSuccessListener {
            val localFile = File.createTempFile("tempImage", "jpg")
            storageReference.child("images/${auth.uid}").getFile(localFile).addOnSuccessListener {
                var bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                bitmap = Bitmap.createScaledBitmap(bitmap, binding.root.findViewById<ImageView>(R.id.nav_head_avatar)!!.width/2, binding.root.findViewById<ImageView>(R.id.nav_head_avatar)!!.height, false)
                img.setImageBitmap(bitmap)
            }
        }

        //Setting names
        val tv1: TextView = headerView.findViewById(R.id.nav_head_username)
        tv1.text = auth.currentUser?.displayName.toString()

        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_skills_list
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setCheckedItem(R.id.nav_skills_list)

        //Enabling listener for hamburger button
        navView.setNavigationItemSelectedListener(this)

        //Setting sign out button
        headerView.findViewById<Button>(R.id.signout_button).setOnClickListener {
            signOut()
            //redirect to main activity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        //Setting user profile
        createProfile()
    }

    private fun createProfile() {
        //Creating a document on firestore database for the new user (first login)
        val docData = hashMapOf(
            "fullname" to auth.currentUser!!.displayName,
            "nickname" to "Your nickname",
            "email" to auth.currentUser!!.email,
            "location" to "Your location",
        )

        db
            .collection("profiles")
            .document(auth.uid.toString())
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null) {
                        if (!document.exists()) {
                            db.collection("profiles").document(auth.uid.toString()).set(docData)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Profile created", Toast.LENGTH_LONG).show()
                                }.addOnFailureListener {
                                    Toast.makeText(this, "Error", Toast.LENGTH_LONG).show()
                                }
                        }
                    }
                }
            }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        val drawerLayout: DrawerLayout = binding.drawerLayout

        if(item.itemId == R.id.nav_adv_list){
            findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.action_nav_skills_list_to_nav_adv_list)
        }
        if(item.itemId == R.id.nav_show_profile){
            findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.action_nav_skills_list_to_nav_show_profile)
        }
        if(item.itemId == R.id.nav_time_slots_saved){
            val bundle = Bundle()
            bundle.putString("flag", "true") //flag for stored timeslots
            findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.action_nav_skills_list_to_nav_adv_list, bundle)
        }
        if(item.itemId == R.id.nav_time_slots_assigned){
            val bundle = Bundle()
            bundle.putString("flag", "trueAssigned") //flag for favourite timeslots
            findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.action_nav_skills_list_to_nav_adv_list, bundle)
        }

        drawerLayout.closeDrawer(GravityCompat.START)

        return true
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)  || super.onSupportNavigateUp()

    }

    override fun onResume() {
        // [START initialize_auth]
        // Initialize Firebase Auth
        auth = Firebase.auth
        // [END initialize_auth]

        //Menù header initialization
        //Setting Image Profile
        val headerView: View = binding.navView.getHeaderView(0)
        val img: ImageView = headerView.findViewById(R.id.nav_head_avatar)
        storageReference.child("images/${auth.uid}").downloadUrl.addOnSuccessListener {
            val localFile = File.createTempFile("tempImage", "jpg")
            storageReference.child("images/${auth.uid}").getFile(localFile).addOnSuccessListener {
                var bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                bitmap = Bitmap.createScaledBitmap(bitmap, binding.root.findViewById<ImageView>(R.id.nav_head_avatar)!!.width/2, binding.root.findViewById<ImageView>(R.id.nav_head_avatar)!!.height, false)
                img.setImageBitmap(bitmap)
            }
        }
        super.onResume()
    }

    private fun signOut() {
        // [START auth_sign_out]
        Firebase.auth.signOut()
        // [END auth_sign_out]
    }


}