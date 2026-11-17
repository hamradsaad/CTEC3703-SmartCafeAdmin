package com.example.smartcafeadmin.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

// Saves menu pictures in the app's private storage (CR-03)
object ImageStorage {

    private const val FOLDER = "menu_images"
    private const val MAX_SIZE = 1024

    // Copies the picked image into private storage, resized and re-saved as JPEG
    // (this also removes metadata such as location). Returns the file path, or null on failure.
    fun saveFromUri(context: Context, uri: Uri): String? {
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, bounds)
            }
            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, MAX_SIZE)
            }
            val bitmap = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            } ?: return null
            saveBitmap(context, bitmap)
        } catch (e: Exception) {
            null
        }
    }

    // Copies a photo bundled inside the app (res/drawable-nodpi) into private storage
    fun saveFromResource(context: Context, resId: Int): String? {
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeResource(context.resources, resId, bounds)
            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, MAX_SIZE)
            }
            val bitmap = BitmapFactory.decodeResource(context.resources, resId, options) ?: return null
            saveBitmap(context, bitmap)
        } catch (e: Exception) {
            null
        }
    }

    private fun saveBitmap(context: Context, bitmap: Bitmap): String {
        val dir = File(context.filesDir, FOLDER)
        dir.mkdirs()
        val file = File(dir, "item_${System.nanoTime()}.jpg")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it) }
        return file.absolutePath
    }

    // Loads a smaller version of the picture for showing on screen
    fun loadThumbnail(path: String, maxSize: Int = 400): Bitmap? {
        if (path.isEmpty()) return null
        val file = File(path)
        if (!file.exists()) return null

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, maxSize)
        }
        return BitmapFactory.decodeFile(path, options)
    }

    fun delete(path: String) {
        if (path.isNotEmpty()) File(path).delete()
    }

    private fun sampleSize(width: Int, height: Int, max: Int): Int {
        var size = 1
        while (width / (size * 2) >= max || height / (size * 2) >= max) {
            size *= 2
        }
        return size
    }
}