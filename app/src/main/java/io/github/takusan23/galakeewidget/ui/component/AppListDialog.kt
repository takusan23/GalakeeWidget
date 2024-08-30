package io.github.takusan23.galakeewidget.ui.component

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

private data class AppData(
    val label: String,
    val applicationId: String
)

@Composable
fun AppListDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    currentApplicationId: String?,
    onSelect: (applicationId: String) -> Unit
) {
    val context = LocalContext.current
    val installAppList = remember { mutableStateOf(emptyList<AppData>()) }

    LaunchedEffect(key1 = Unit) {
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfoList = context.packageManager.queryIntentActivities(mainIntent, 0)
        installAppList.value = resolveInfoList.map { info ->
            AppData(
                label = info.loadLabel(context.packageManager).toString(),
                applicationId = info.activityInfo.packageName
            )
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = modifier) {
            Text(
                text = "アプリを選ぶ",
                fontSize = 24.sp
            )

            LazyColumn {
                items(installAppList.value) { appData ->
                    Row(
                        modifier = Modifier
                            .padding(5.dp)
                            .fillMaxWidth()
                            .clickable { onSelect(appData.applicationId) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = appData.label, fontSize = 20.sp)
                            Text(text = appData.applicationId)
                        }
                        if (appData.applicationId == currentApplicationId) {
                            Text(
                                modifier = Modifier.padding(start = 10.dp),
                                text = "[選択中]"
                            )
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}