package com.example.projetopratico_mobile1.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.InputStream
import java.io.FileOutputStream

/**
 * Gerencia armazenamento local de imagens das listas
 * Salva no diretório interno do app (não precisa de permissões)
 */
object LocalImageStore {

    /**
     * Retorna o arquivo onde deve ficar a imagem da lista
     */
    fun fileForList(context: Context, listId: String): File {
        val imagesDir = File(context.applicationContext.filesDir, "images")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
        return File(imagesDir, "${listId}.img")
    }

    /**
     * Salva uma imagem a partir de um content:// URI
     * Retorna true se salvou com sucesso
     */
    fun saveFromContentUri(context: Context, listId: String, contentUriString: String): Boolean {
        return try {
            val uri = Uri.parse(contentUriString)
            val inputStream: InputStream? = context.applicationContext.contentResolver.openInputStream(uri)

            if (inputStream == null) return false

            val targetFile = fileForList(context.applicationContext, listId)
            val outputStream = FileOutputStream(targetFile)

            // Copiar bytes em buffer de 8KB
            val buffer = ByteArray(8 * 1024)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            inputStream.close()
            outputStream.close()

            true
        } catch (e: Exception) {
            // SecurityException, IOException etc. - falhar silenciosamente
            false
        }
    }

    /**
     * Verifica se existe imagem salva para a lista
     */
    fun exists(context: Context, listId: String): Boolean {
        return fileForList(context.applicationContext, listId).exists()
    }

    /**
     * Remove a imagem da lista (ao excluir lista)
     */
    fun deleteForList(context: Context, listId: String) {
        try {
            fileForList(context.applicationContext, listId).delete()
        } catch (e: Exception) {
            // Ignorar erro ao deletar
        }
    }
}
