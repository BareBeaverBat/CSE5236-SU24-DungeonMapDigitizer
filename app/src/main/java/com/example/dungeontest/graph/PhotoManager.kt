package com.example.dungeontest.graph

import android.content.Context
import android.util.Base64
import java.io.File
import java.io.FileOutputStream

fun saveImageToInternalStorage(context: Context, base64Image: String, mapName: String): String? {
    return try {
        val imageBytes = Base64.decode(base64Image, Base64.DEFAULT)
        val file = File(context.filesDir, "$mapName-original.jpg")
        FileOutputStream(file).use { fos ->
            fos.write(imageBytes)
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun deletePhotoFromInternalStorage(context: Context, mapName: String): Boolean? {
    return try {
        val file = File(context.filesDir, "$mapName-original.jpg")
        if (file.exists()) {
            file.delete()
        } else {
            false
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}