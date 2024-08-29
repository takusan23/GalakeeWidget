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
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

class GarakeeWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val routing = remember { mutableStateOf(Routing.Standby) }

            GlanceTheme {
                when (routing.value) {
                    Routing.Standby -> StandbyScreen(context)
                    Routing.Menu -> MenuScreen(context)
                }
            }
        }
    }

    /** 待ち受け画面 */
    @Composable
    private fun StandbyScreen(context: Context) {

        val dateData = remember { DateTool.createDateData() }

        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.primaryContainer)
        ) {

            Column(
                modifier = GlanceModifier
                    .padding(10.dp)
                    .defaultWeight()
                    .fillMaxHeight(),
            ) {
                Text(
                    text = dateData.time,
                    style = TextStyle(fontSize = 20.sp)
                )

                Spacer(GlanceModifier.height(5.dp))
                Text(
                    text = dateData.date,
                    style = TextStyle(fontSize = 16.sp)
                )

                Spacer(GlanceModifier.height(5.dp))
                Text(
                    text = dateData.calender,
                    style = TextStyle(fontFamily = FontFamily.Monospace)
                )
            }

            GarakeeKeysUi()
        }
    }

    /** メニュー画面 */
    @Composable
    private fun MenuScreen(context: Context) {
        val suggestAppList = remember { mutableStateOf(emptyList<AppListTool.AppInfoData>()) }

        LaunchedEffect(key1 = Unit) {
            suggestAppList.value = AppListTool.querySuggestAppList(
                context = context,
                timeMachineDateCount = -1
            )
        }

        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.primaryContainer)
        ) {

            if (suggestAppList.value.isEmpty()) {
                // 読み込み中
                Box(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyVerticalGrid(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .fillMaxHeight(),
                    gridCells = GridCells.Fixed(3)
                ) {
                    items(suggestAppList.value) { appInfo ->
                        Column(
                            modifier = GlanceModifier.clickable(actionStartActivity(appInfo.intent)),
                            verticalAlignment = Alignment.Vertical.CenterVertically,
                            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                        ) {
                            Image(
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .height(50.dp),
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

            GarakeeKeysUi()
        }
    }

    /** ボタンとかがある部分のコンポーネント。幅 120dp くらい使います。 */
    @Composable
    private fun GarakeeKeysUi(modifier: GlanceModifier = GlanceModifier) {
        Row(
            modifier = modifier
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

    enum class Routing {
        Standby,
        Menu
    }

}