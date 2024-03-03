package lib.asyncimage

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import okhttp3.HttpUrl
import java.io.File
import java.io.FileOutputStream
import java.util.Base64


internal class CacheUtils {

    fun readCache(networkUrl: HttpUrl): ByteArray? {
        val cacheFile = File(
            cacheDir(application = application),
            networkUrlToCacheFilename(networkUrl)
        )
        return if (cacheFile.exists()) {
            if (debug) println("CacheUtils: readCache")
            cacheFile.readBytes()
        } else {
            null
        }
    }

    fun writeCache(networkUrl: HttpUrl, data: ByteArray): File? {
        val cacheFile = File(
            cacheDir(application = application),
            networkUrlToCacheFilename(networkUrl)
        )

        FileOutputStream(cacheFile).use {
            it.write(data)
        }

        return if (cacheFile.exists()) {
            if (debug) println("CacheUtils: writeCache")
            cacheFile
        } else {
            null
        }
    }

    companion object {
        var debug = true
        lateinit var application: Application
        private const val base64StringSkipTrailingCount = 15

        val placeholderBitmap = Bitmap.createBitmap(120, 90, Bitmap.Config.RGB_565)
            get() {
                field.eraseColor(Color.GRAY)
                return field
            }

        private fun cacheDir(application: Application): File {
            val folder = File(application.cacheDir,"ImageCache")
            if (!folder.exists()) {
                folder.mkdir()
            }
            return folder
        }

        private fun networkUrlToCacheFilename(url: HttpUrl): String {
            val urlAsBytes = url.toString().encodeToByteArray()
            val encodedUrlString = Base64.getUrlEncoder().encodeToString(urlAsBytes)
            return encodedUrlString.substring(
                0, encodedUrlString.count() - base64StringSkipTrailingCount
            )
        }

        fun listCache() {
            if (debug) {
                val files = mutableListOf<String>()
                cacheDir(application = application).listFiles()?.toString()?.let {
                    files.add(it)
                }
                println("CacheUtils: listCache")
                println(files.joinToString(separator = ","))
            }
        }
    }
}