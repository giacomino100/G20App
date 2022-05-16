package it.polito.g20app

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.*


class Photo {

    //Save Image
    fun saveToInternalStorage(bitmapImage : Bitmap, cw : ContextWrapper, dir : String, imageName : String ): String {

        // path to /data/data/yourApp/app_data/imageDir
        val directory : File = cw.getDir(dir, Context.MODE_PRIVATE)

        // Create imageDir
        val myPath = File(directory, imageName)
        var fos : FileOutputStream? = null

        try {
            fos = FileOutputStream(myPath)

            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos)

        }
        catch (e: Exception) {
            //e.printStackTrace()

            //Check for it
            Log.i("error", "image can't be saved!")
        }
        finally {

            //Close the stream
            try {
                fos?.close()
            }
            catch (e: IOException) {

                //e.printStackTrace()
                Log.i("error", "stream can't be closed!")
            }
        }

        //Return the path
        return directory.absolutePath
    }

    //Retrieve Image
    fun loadImageFromStorage(path: String?, imageName : String) : Bitmap? {
        //Try to get the file from the bitmap
        try {
            //Get the file and load the bitmap
            val f = File(path, imageName)
            val b = BitmapFactory.decodeStream(FileInputStream(f))
            // With create scaled bitmap we create a bitmap with correct measures
            return Bitmap.createScaledBitmap(b, b.width*5, b.height*5, false)
        }
        catch (e: FileNotFoundException) {
            //e.printStackTrace()
            Log.i("error", "file not found!")
        }
        return null
    }

}