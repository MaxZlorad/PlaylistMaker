package com.practicum.playlistmaker.library.data.repository

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.practicum.playlistmaker.library.domain.impl.ImageStorage
import java.io.File
import java.io.FileOutputStream

class ImageStorageImpl(private val context: Context) : ImageStorage {

    override fun saveImage(uri: Uri): String? {
        return try {
            val playlistCoversDir = File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "playlist_covers"
            )

            if (!playlistCoversDir.exists()) {
                playlistCoversDir.mkdirs()
            }

            val fileName = "cover_${System.currentTimeMillis()}.jpg"
            val file = File(playlistCoversDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}