package io.github.takusan23.galakeewidget.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import io.github.takusan23.galakeewidget.tool.DataStore
import io.github.takusan23.galakeewidget.tool.DataStore.getShortcutAppIdData
import io.github.takusan23.galakeewidget.tool.DataStore.getWallpaperUriList
import io.github.takusan23.galakeewidget.tool.DataStore.saveShortcutAppIdData
import io.github.takusan23.galakeewidget.tool.DataStore.saveWallpaperUriList
import io.github.takusan23.galakeewidget.tool.dataStore
import io.github.takusan23.galakeewidget.ui.component.AppListDialog
import io.github.takusan23.galakeewidget.ui.component.WallpaperPreview
import kotlinx.coroutines.launch

@Composable
fun MainScreen() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val currentWallpaperUriList = remember { mutableStateOf(emptyList<Uri>()) }
    val visibleDialogShortcutAppIndex = remember { mutableStateOf<Int?>(null) }
    val shortcutAppIdData = remember { mutableStateOf<DataStore.ShortcutAppIdData?>(null) }

    fun save() {
        scope.launch {
            context.dataStore.edit {
                it.saveWallpaperUriList(currentWallpaperUriList.value)
            }
        }
    }

    // 壁紙選ぶやつ
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uriList ->
            // Uri を DataStore に保存する場合、永続化が必要なので
            uriList.forEach { uri ->
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                currentWallpaperUriList.value += uri
            }
            save()
        }
    )

    LaunchedEffect(key1 = Unit) {
        context.dataStore.data.collect {
            currentWallpaperUriList.value = it.getWallpaperUriList()
            shortcutAppIdData.value = it.getShortcutAppIdData()
        }
    }

    // ショートカットアプリ設定ダイアログ
    if (visibleDialogShortcutAppIndex.value != null) {
        AppListDialog(
            onDismiss = { visibleDialogShortcutAppIndex.value = null },
            currentApplicationId = when (visibleDialogShortcutAppIndex.value) {
                0 -> shortcutAppIdData.value?.one
                1 -> shortcutAppIdData.value?.two
                2 -> shortcutAppIdData.value?.three
                else -> null
            },
            onSelect = { applicationId ->
                scope.launch {
                    // 保存する
                    shortcutAppIdData.value = when (visibleDialogShortcutAppIndex.value) {
                        0 -> shortcutAppIdData.value?.copy(one = applicationId)
                        1 -> shortcutAppIdData.value?.copy(two = applicationId)
                        2 -> shortcutAppIdData.value?.copy(three = applicationId)
                        else -> null
                    }
                    context.dataStore.edit { it.saveShortcutAppIdData(shortcutAppIdData.value!!) }
                    // 消す
                    visibleDialogShortcutAppIndex.value = null
                }
            }
        )
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(10.dp)
        ) {


            Button(onClick = { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }) {
                Text(text = "利用状況へのアクセス権限")
            }
            Button(onClick = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }) {
                Text(text = "通知領域へのアクセス権限")
            }

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

            Text(text = "ショートカットキー")
            Column {
                listOf(
                    "ⅰ" to shortcutAppIdData.value?.one,
                    "ⅱ" to shortcutAppIdData.value?.two,
                    "ⅲ" to shortcutAppIdData.value?.three
                ).forEachIndexed { index, (text, appId) ->
                    Button(onClick = { visibleDialogShortcutAppIndex.value = index }) {
                        Text(text = "$text = $appId")
                    }
                }
            }
        }
    }
}
