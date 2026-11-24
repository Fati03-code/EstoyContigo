package mx.ipn.upiicsa.estoycontigo.platform

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

/** Fotos en almacenamiento interno privado (no visible para otras apps ni la galería). */
class AndroidPhotoStorage(context: Context) : PhotoStorage {
    private val dir = File(context.filesDir, "photos").apply { mkdirs() }

    override suspend fun readBytes(path: String): ByteArray? = withContext(Dispatchers.IO) {
        File(path).takeIf { it.exists() }?.readBytes()
    }

    override suspend fun save(fileName: String, bytes: ByteArray): String = withContext(Dispatchers.IO) {
        val file = File(dir, fileName)
        file.writeBytes(bytes)
        file.absolutePath
    }

    override suspend fun delete(path: String) {
        withContext(Dispatchers.IO) { File(path).delete() }
    }

    override suspend fun deleteAll() {
        withContext(Dispatchers.IO) { dir.listFiles()?.forEach { it.delete() } }
    }

    /** Reduce la foto a máx. 1280 px, corrige orientación EXIF y elimina metadatos (incl. GPS). */
    suspend fun compressFrom(source: File, fileName: String): String? = withContext(Dispatchers.IO) {
        if (!source.exists() || source.length() == 0L) return@withContext null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(source.absolutePath, bounds)
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SIDE) sample *= 2
        val decoded = BitmapFactory.decodeFile(source.absolutePath, BitmapFactory.Options().apply { inSampleSize = sample })
            ?: return@withContext null
        val rotation = when (ExifInterface(source.absolutePath).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        val scale = MAX_SIDE.toFloat() / maxOf(decoded.width, decoded.height)
        val matrix = Matrix().apply {
            if (scale < 1f) postScale(scale, scale)
            postRotate(rotation)
        }
        val output = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
        val bytes = ByteArrayOutputStream().use { out ->
            output.compress(Bitmap.CompressFormat.JPEG, 80, out)
            out.toByteArray()
        }
        source.delete()
        save(fileName, bytes)
    }

    private companion object {
        const val MAX_SIDE = 1280
    }
}
