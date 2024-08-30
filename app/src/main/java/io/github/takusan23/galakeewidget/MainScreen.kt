package io.github.takusan23.galakeewidget

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import com.bumptech.glide.Glide
import io.github.takusan23.galakeewidget.DataStore.getWallpaperUriList
import io.github.takusan23.galakeewidget.DataStore.saveWallpaperUriList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MainScreen() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val currentWallpaperUriList = remember { mutableStateOf(emptyList<Uri>()) }

    fun save() {
        scope.launch {
            context.dataStore.edit {
                it.saveWallpaperUriList(currentWallpaperUriList.value)
            }
        }
    }

    // 壁紙選ぶやつ
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri ?: return@rememberLauncherForActivityResult
            // Uri を DataStore に保存する場合、永続化が必要なので
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            currentWallpaperUriList.value += uri
            save()
        }
    )

    LaunchedEffect(key1 = Unit) {
        context.dataStore.data.collect {
            currentWallpaperUriList.value = it.getWallpaperUriList()
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(10.dp)
        ) {


            Text(text = "待ち受け画面")
            Button(onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                Text(text = "壁紙を追加")
            }
            LazyRow {
                items(currentWallpaperUriList.value) { uri ->
                    WallpaperPreview(
                        modifier = Modifier.size(100.dp),
                        uri = uri,
                        onDelete = {
                            currentWallpaperUriList.value -= uri
                            context.contentResolver.releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            save()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun WallpaperPreview(
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