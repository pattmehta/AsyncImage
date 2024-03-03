package lib.asyncimage

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.concurrent.TimeUnit

private val asyncImageLoader = AsyncImageLoader().build()

@Composable
fun AsyncImage(urlString: String, modifier: Modifier, scaleFactor: Float = 1f) {
    CacheUtils.application = LocalContext.current.applicationContext as Application
    val coroutineScope = rememberCoroutineScope()
    val imageUrl = HttpUrl.get(urlString)
    val bitmapState = remember { mutableStateOf(AsyncImageLoader.whiteBitmap) }

    LaunchedEffect(true) {
        coroutineScope.launch(Dispatchers.IO) {
            asyncImageLoader.loadBitmap(coroutineScope, bitmapState, imageUrl)
        }
    }

    // clipToBounds is important for scaleFactor, as if
    // scaleFactor > 1, and if clipToBounds is not used
    // the image will overflow the box dimensions
    Box(modifier = modifier.clipToBounds()) {
        Image(
            bitmap = bitmapState.value.asImageBitmap(),
            contentDescription = "image ${File(urlString).name}",
            modifier = modifier.scale(scaleFactor),
            contentScale = ContentScale.FillHeight
        )
    }
}

class AsyncImageLoader {

    companion object {
        var debug = false
            set(value) {
                field = value
                CacheUtils.debug = value
                CacheUtils.listCache()
            }

        val whiteBitmap = Bitmap.createBitmap(120, 90, Bitmap.Config.RGB_565)
            get() {
                field.eraseColor(Color.WHITE)
                return field
            }

        fun bitmapFromByteArray(byteArray: ByteArray): Bitmap =
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    private var didBuild = false
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var cacheUtils: CacheUtils

    internal fun build(): AsyncImageLoader {
        if (!didBuild) {
            // okhttp caching has issues in offline mode
            var builder = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
            if (debug) {
                val logging = HttpLoggingInterceptor()
                    .setLevel(HttpLoggingInterceptor.Level.BODY)
                builder = builder.addInterceptor(logging)
            }
            okHttpClient = builder.build()
            cacheUtils = CacheUtils()
            didBuild = true
        }
        return this
    }

    internal fun loadBitmap(
        coroutineScope: CoroutineScope,
        bitmapState: MutableState<Bitmap>,
        url: HttpUrl
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            delay(500)
            cacheUtils.readCache(url)?.let {
                bitmapState.value = bitmapFromByteArray(it)
                return@launch
            }
            delay(500)
            val data = dataFromUrl(url)
            delay(500)
            if (data != null) {
                if (cacheUtils.writeCache(url, data) != null) {
                    bitmapState.value = bitmapFromByteArray(data)
                    return@launch
                }
            }
            bitmapState.value = CacheUtils.placeholderBitmap
        }
    }

    private fun dataFromUrl(url: HttpUrl): ByteArray? {
        try {
            val request = Request.Builder().url(url).build()
            val call = okHttpClient.newCall(request)
            val response = call.execute()
            response.use {
                if (it.isSuccessful) {
                    it.body()?.let { responseBody ->
                        val inputStream = responseBody.byteStream()
                        inputStream.use { stream ->
                            return stream.readBytes()
                        }
                    }
                } else {
                    println("AsyncImageLoader: !isSuccessful ${it.code()}:${it.message()}")
                }
            }
            return null
        } catch (exception: Exception) {
            println("AsyncImageLoader: exception")
            // IOException, UnknownHostException, IllegalStateException
            return null
        }
    }
}