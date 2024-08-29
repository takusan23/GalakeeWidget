package io.github.takusan23.galakeewidget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.GridCells
import androidx.glance.appwidget.lazy.LazyVerticalGrid
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

class GarakeeWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                MenuScreen(context)
            }
        }
    }

    @Composable
    private fun MenuScreen(context: Context) {
        val suggestAppList = remember { mutableStateOf(emptyList<AppListTool.AppInfoData>()) }

        LaunchedEffect(key1 = Unit) {
            suggestAppList.value = AppListTool.querySuggestAppList(
                context = context,
                timeMachineDateCount = -1
            )
        }

        GarakeeScaffold {
            if (suggestAppList.value.isEmpty()) {
                // 読み込み中
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyVerticalGrid(gridCells = GridCells.Fixed(4)) {
                    items(suggestAppList.value) { appInfo ->
                        Column(
                            modifier = GlanceModifier.clickable(actionStartActivity(appInfo.intent)),
                            verticalAlignment = Alignment.Vertical.CenterVertically,
                            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                        ) {
                            Image(
                                modifier = GlanceModifier.padding(horizontal = 7.dp),
                                provider = ImageProvider(appInfo.icon),
                                contentDescription = null
                            )
                            Text(
                                text = appInfo.label,
                                style = TextStyle(fontSize = 12.sp),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun GarakeeScaffold(
        modifier: GlanceModifier = GlanceModifier,
        main: @Composable () -> Unit
    ) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(GlanceTheme.colors.primaryContainer)
        ) {

            Box(
                modifier = GlanceModifier.defaultWeight(),
                content = main
            )

            Row(
                modifier = GlanceModifier
                    .padding(5.dp)
                    .background(GlanceTheme.colors.secondaryContainer)
            ) {

                Column(
                    modifier = GlanceModifier
                        .fillMaxHeight()
                        .width(80.dp)
                ) {
                    Text(
                        text = "12:34",
                        style = TextStyle(fontSize = 18.sp)
                    )

                    Spacer(GlanceModifier.defaultWeight())

                    Spacer(GlanceModifier.height(5.dp))
                    GarakeeButton(
                        modifier = GlanceModifier.fillMaxWidth(),
                        onClick = {}
                    ) {
                        Text(text = "ﾒﾆｭｰ")
                    }

                    Spacer(GlanceModifier.height(5.dp))
                    GarakeeButton(
                        modifier = GlanceModifier.fillMaxWidth(),
                        onClick = {}
                    ) {
                        Text(text = "閉じる")
                    }
                }

                Spacer(GlanceModifier.width(5.dp))

                Column(
                    modifier = GlanceModifier
                        .fillMaxHeight()
                        .width(40.dp)
                ) {
                    listOf("ⅰ", "ⅱ", "ⅲ").forEach { text ->

                        Spacer(GlanceModifier.height(5.dp))
                        GarakeeButton(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .defaultWeight(),
                            onClick = {}
                        ) {
                            Text(
                                text = text,
                                style = TextStyle(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun GarakeeButton(
        modifier: GlanceModifier = GlanceModifier,
        onClick: () -> Unit,
        content: @Composable () -> Unit
    ) {
        Box(
            modifier = modifier
                .background(GlanceTheme.colors.surface)
                .padding(10.dp)
                .cornerRadius(5.dp)
                .clickable(onClick),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}