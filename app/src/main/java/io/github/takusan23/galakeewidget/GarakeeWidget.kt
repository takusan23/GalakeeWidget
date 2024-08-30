package io.github.takusan23.galakeewidget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.action
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
import androidx.glance.layout.ContentScale
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
import androidx.glance.unit.ColorProvider
import com.bumptech.glide.Glide
import io.github.takusan23.galakeewidget.tool.AppListTool
import io.github.takusan23.galakeewidget.tool.DataStore
import io.github.takusan23.galakeewidget.tool.DataStore.getShortcutAppIdData
import io.github.takusan23.galakeewidget.tool.DataStore.getWallpaperUriList
import io.github.takusan23.galakeewidget.tool.DateTool
import io.github.takusan23.galakeewidget.tool.dataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class GarakeeWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val routing = remember { mutableStateOf(Routing.Standby) }
            val shortcutAppIdData = produceState<DataStore.ShortcutAppIdData?>(
                initialValue = null,
                producer = { context.dataStore.data.collect { this.value = it.getShortcutAppIdData() } }
            )

            GlanceTheme {
                when (routing.value) {
                    Routing.Standby -> StandbyScreen(
                        context = context,
                        onMenuClick = action { routing.value = Routing.Menu },
                        onCloseClick = action { routing.value = Routing.Standby },
                        onOneClick = actionStartActivity(context, applicationId = shortcutAppIdData.value?.one),
                        onTwoClick = actionStartActivity(context, applicationId = shortcutAppIdData.value?.two),
                        onThreeClick = actionStartActivity(context, applicationId = shortcutAppIdData.value?.three)
                    )

                    Routing.Menu -> MenuScreen(
                        context = context,
                        onMenuClick = action { routing.value = Routing.Standby },
                        onCloseClick = action { routing.value = Routing.Standby },
                        onOneClick = actionStartActivity(context, applicationId = shortcutAppIdData.value?.one),
                        onTwoClick = actionStartActivity(context, applicationId = shortcutAppIdData.value?.two),
                        onThreeClick = actionStartActivity(context, applicationId = shortcutAppIdData.value?.three)
                    )
                }
            }
        }
    }

    /** 待ち受け画面 */
    @Composable
    private fun StandbyScreen(
        context: Context,
        onMenuClick: Action,
        onCloseClick: Action,
        onOneClick: Action,
        onTwoClick: Action,
        onThreeClick: Action
    ) {
        val dateData = remember { DateTool.createDateData() }
        val wallpaperBitmap = remember { mutableStateOf<Bitmap?>(null) }

        // 待ち受け画面の壁紙をロードする、ランダムで取り出して小さくしてから
        LaunchedEffect(key1 = Unit) {
            val uri = context.dataStore.data.first().getWallpaperUriList().randomOrNull() ?: return@LaunchedEffect
            wallpaperBitmap.value = withContext(Dispatchers.IO) {
                Glide.with(context)
                    .asBitmap()
                    .load(uri)
                    .submit(400, 400)
                    .get()
            }
        }

        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .then(
                    if (wallpaperBitmap.value != null) {
                        GlanceModifier.background(
                            imageProvider = ImageProvider(wallpaperBitmap.value!!),
                            contentScale = ContentScale.Crop,
                            colorFilter = ColorFilter.tint(ColorProvider(Color.Black.copy(0.3f)))
                        )
                    } else {
                        GlanceModifier.background(
                            colorProvider = ColorProvider(Color.Black.copy(0.3f))
                        )
                    }
                )
        ) {

            Column(
                modifier = GlanceModifier
                    .padding(10.dp)
                    .defaultWeight()
                    .fillMaxHeight(),
            ) {

                Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
                    Text(
                        text = dateData.time,
                        style = TextStyle(
                            fontSize = 24.sp,
                            color = GlanceTheme.colors.primaryContainer
                        )
                    )

                    Spacer(GlanceModifier.width(10.dp))
                    Text(
                        text = dateData.date,
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = GlanceTheme.colors.primaryContainer
                        )
                    )
                }

                Text(
                    text = dateData.calender,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        color = GlanceTheme.colors.primaryContainer
                    )
                )
            }

            GarakeeKeysUi(
                modifier = GlanceModifier,
                onMenuClick = onMenuClick,
                onCloseClick = onCloseClick,
                onOneClick = onOneClick,
                onTwoClick = onTwoClick,
                onThreeClick = onThreeClick
            )
        }
    }

    /** メニュー画面 */
    @Composable
    private fun MenuScreen(
        context: Context,
        onMenuClick: Action,
        onCloseClick: Action,
        onOneClick: Action,
        onTwoClick: Action,
        onThreeClick: Action
    ) {
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

            GarakeeKeysUi(
                modifier = GlanceModifier.background(GlanceTheme.colors.secondaryContainer),
                onMenuClick = onMenuClick,
                onCloseClick = onCloseClick,
                onOneClick = onOneClick,
                onTwoClick = onTwoClick,
                onThreeClick = onThreeClick
            )
        }
    }

    /** ボタンとかがある部分のコンポーネント。幅 100dp くらい使います。 */
    @Composable
    private fun GarakeeKeysUi(
        modifier: GlanceModifier = GlanceModifier,
        onMenuClick: Action,
        onCloseClick: Action,
        onOneClick: Action,
        onTwoClick: Action,
        onThreeClick: Action
    ) {
        Row(modifier = modifier.padding(5.dp)) {

            Column(
                modifier = GlanceModifier
                    .fillMaxHeight()
                    .width(70.dp)
            ) {

                // TODO アイコン自前で書きたい
                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    Image(
                        modifier = GlanceModifier.defaultWeight(),
                        provider = ImageProvider(R.drawable.battery_3_bar_24px),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.primary)
                    )
                    Image(
                        modifier = GlanceModifier.defaultWeight(),
                        provider = ImageProvider(R.drawable.signal_cellular_alt_24px),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.primary)
                    )
                }

                Text(
                    text = "12:34",
                    style = TextStyle(fontSize = 18.sp)
                )

                Spacer(GlanceModifier.defaultWeight())

                Spacer(GlanceModifier.height(5.dp))
                GarakeeButton(
                    modifier = GlanceModifier.fillMaxWidth(),
                    onClick = onMenuClick
                ) {
                    Text(
                        text = "ﾒﾆｭｰ",
                        style = TextStyle(fontSize = 16.sp)
                    )
                }

                Spacer(GlanceModifier.height(5.dp))
                GarakeeButton(
                    modifier = GlanceModifier.fillMaxWidth(),
                    onClick = onCloseClick
                ) {
                    Image(
                        modifier = GlanceModifier.padding(horizontal = 5.dp).fillMaxWidth(),
                        provider = ImageProvider(R.drawable.call_end_24px),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.error)
                    )
                }
            }

            Spacer(GlanceModifier.width(5.dp))

            Column(
                modifier = GlanceModifier
                    .fillMaxHeight()
                    .width(40.dp)
            ) {
                listOf(
                    "ⅰ" to onOneClick,
                    "ⅱ" to onTwoClick,
                    "ⅲ" to onThreeClick
                ).forEach { (text, onClick) ->

                    Spacer(GlanceModifier.height(5.dp))
                    GarakeeButton(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .defaultWeight(),
                        onClick = onClick
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
        onClick: Action,
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

    /** [actionStartActivity]のアプリケーション ID 指定版 */
    private fun actionStartActivity(
        context: Context,
        applicationId: String?
    ): Action = actionStartActivity(
        intent = if (applicationId == null) {
            Intent()
        } else {
            context.packageManager.getLaunchIntentForPackage(applicationId)!!
        }
    )

    enum class Routing {
        Standby,
        Menu
    }

}