package com.example.dungeontest.graph

import android.content.Context
import java.io.File
import java.io.FileOutputStream

fun saveImageToInternalStorage(context: Context, imageBytes: ByteArray, mapName: String): String? {
    return try {
        val file = File(context.filesDir, "$mapName-original.jpeg")
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
        val file = File(context.filesDir, "$mapName-original.jpeg")
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