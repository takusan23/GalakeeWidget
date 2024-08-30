package io.github.takusan23.galakeewidget.ui.component

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun WallpaperPreview(
    modifier: Modifier = Modifier,
    uri: Uri,
    onDelete: (Uri) -> Unit
) {
    val context = LocalContext.current
    val bitmap = remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(key1 = Unit) {
        bitmap.value = withContext(Dispatchers.IO) {
            Glide.with(context)
                .asBitmap()
                .load(uri)
                .submit(400, 400)
                .get()
                .asImageBitmap()
        }
    }

    Column(modifier) {

        if (bitmap.value != null) {
            Image(bitmap = bitmap.value!!, contentDescription = null)
        }

        Button(onClick = { onDelete(uri) }) {
            Text(text = "削除")
        }
    }

}