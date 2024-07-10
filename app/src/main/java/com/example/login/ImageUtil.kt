package com.example.login

import android.content.Context
import android.graphics.Bitmap
import java.io.FileOutputStream
import java.io.IOException

object ImageUtil {
    fun saveImageToInternalStorage(context: Context, bitmap: Bitmap, imageName: String): String? {
        return try {
            val fos: FileOutputStream = context.openFileOutput(imageName, Context.MODE_PRIVATE)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()
            context.getFileStreamPath(imageName).absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}