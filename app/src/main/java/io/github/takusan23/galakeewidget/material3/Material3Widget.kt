package io.github.takusan23.galakeewidget.material3

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.itemsIndexed
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
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import io.github.takusan23.galakeewidget.R
import io.github.takusan23.galakeewidget.tool.AppListTool
import io.github.takusan23.galakeewidget.tool.DataStore
import io.github.takusan23.galakeewidget.tool.DataStore.getShortcutAppIdData
import io.github.takusan23.galakeewidget.tool.DataStore.getWallpaperUriList
import io.github.takusan23.galakeewidget.tool.dataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat

class Material3Widget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                MainScreen(context)
            }
        }
    }

    @Composable
    private fun MainScreen(context: Context) {
        val shortcutAppIdData = produceState<DataStore.ShortcutAppIdData?>(
            initialValue = null,
            producer = { context.dataStore.data.collect { this.value = it.getShortcutAppIdData() } }
        )
        val wallpaperBitmap = remember { mutableStateOf<Bitmap?>(null) }

        val suggestAppList = remember { mutableStateOf(emptyList<AppListTool.AppInfoData>()) }
        LaunchedEffect(key1 = Unit) {
            suggestAppList.value = AppListTool.querySuggestAppList(
                context = context,
                timeMachineDateCount = -1
            )
        }

        LaunchedEffect(key1 = Unit) {
            val preference = context.dataStore.data.first()

            // 待ち受け画面の壁紙をロードする、ランダムで取り出して小さくしてから
            val uri = preference.getWallpaperUriList().randomOrNull() ?: return@LaunchedEffect
            /*
                        wallpaperBitmap.value = withContext(Dispatchers.IO) {
                            val loader = ImageLoader(context)
                            val request = ImageRequest.Builder(context)
                                .data(uri)
                                .transformations(RoundedCornersTransformation(20f))
                                .size(Dimension.Undefined, Dimension.Pixels(500))
                                .build()
                            val imageResult = loader.execute(request)
                            imageResult.image?.toBitmap()
                        }
            */

            wallpaperBitmap.value = withContext(Dispatchers.Default) {
                Glide.with(context)
                    .asBitmap()
                    .load(uri)
                    .apply(RequestOptions.bitmapTransform(RoundedCorners(20)))
                    .submit(400, 400)
                    .get()
            }
        }

        Row(
            modifier = GlanceModifier.fillMaxSize().padding(top = 10.dp),
            // verticalAlignment = Alignment.CenterVertically
        ) {

            Column(modifier = GlanceModifier.defaultWeight()) {

                if (wallpaperBitmap.value != null) {
                    Image(
                        modifier = GlanceModifier,
                        provider = ImageProvider(wallpaperBitmap.value!!),
                        contentDescription = null
                    )
                }

                BottomBar()
            }

            Spacer(modifier = GlanceModifier.width(5.dp))

            LazyColumn(modifier = GlanceModifier.width(100.dp).fillMaxHeight()) {
                item {
                    if (shortcutAppIdData.value != null) {
                        QsView(
                            context = context,
                            shortcutAppIdData = shortcutAppIdData.value!!
                        )
                    }
                }

                itemsIndexed(suggestAppList.value) { index, appInfoData ->
                    AppList(
                        modifier = GlanceModifier,
                        context = context,
                        appInfoData = appInfoData,
                        index = index,
                        size = suggestAppList.value.size
                    )
                }
            }
        }
    }

    @Composable
    private fun AppList(
        modifier: GlanceModifier = GlanceModifier,
        context: Context,
        appInfoData: AppListTool.AppInfoData,
        index: Int,
        size: Int
    ) {
        Column(modifier = modifier) {
            val content = @Composable {
                Row(
                    modifier = GlanceModifier
                        .clickable(actionStartActivity(context, applicationId = appInfoData.packageName))
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = GlanceModifier
                            .background(GlanceTheme.colors.primary)
                            .cornerRadius(10.dp)
                    ) {
                        Image(
                            modifier = GlanceModifier.size(25.dp),
                            provider = ImageProvider(appInfoData.icon),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(GlanceTheme.colors.primaryContainer)
                        )
                    }
                    Text(
                        modifier = GlanceModifier.defaultWeight().padding(start = 5.dp),
                        text = appInfoData.label,
                        style = TextStyle(color = GlanceTheme.colors.primary)
                    )
                }
            }

            if (index != 0) {
                Spacer(modifier = GlanceModifier.height(3.dp))
            }

            when (index) {
                0 -> RoundedCornerBox(
                    modifier = GlanceModifier,
                    color = GlanceTheme.colors.background,
                    radius = 10.dp,
                    disableList = listOf(Corner.BOTTOM_LEFT, Corner.BOTTOM_RIGHT),
                    contentAlignment = Alignment.Center,
                    content = content
                )

                size - 1 -> RoundedCornerBox(
                    modifier = GlanceModifier,
                    color = GlanceTheme.colors.background,
                    radius = 10.dp,
                    disableList = listOf(Corner.TOP_LEFT, Corner.TOP_RIGHT),
                    contentAlignment = Alignment.Center,
                    content = content
                )

                else -> RoundedCornerBox(
                    modifier = GlanceModifier,
                    color = GlanceTheme.colors.background,
                    radius = 10.dp,
                    disableList = Corner.entries,
                    contentAlignment = Alignment.Center,
                    content = content
                )
            }
        }
    }

    @Composable
    private fun QsView(
        modifier: GlanceModifier = GlanceModifier,
        context: Context,
        shortcutAppIdData: DataStore.ShortcutAppIdData
    ) {
        Column(modifier = modifier) {
            QsMenu(
                modifier = GlanceModifier.fillMaxWidth(),
                title = "Wi-Fi",
                iconResId = R.drawable.android_galakeewidget_wifi,
                action = actionStartActivity(context, applicationId = shortcutAppIdData.one)
            )

            Spacer(modifier = GlanceModifier.height(5.dp))
            QsMenu(
                modifier = GlanceModifier.fillMaxWidth(),
                title = "5G",
                iconResId = R.drawable.android_galakeewidget_antenna_3,
                action = actionStartActivity(context, applicationId = shortcutAppIdData.two)
            )

            Spacer(modifier = GlanceModifier.height(5.dp))
            QsMenu(
                modifier = GlanceModifier.fillMaxWidth(),
                title = "100%",
                iconResId = R.drawable.android_galakeewidget_battery_3,
                action = actionStartActivity(context, applicationId = shortcutAppIdData.three)
            )

            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(5.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = GlanceModifier
                        .width(25.dp)
                        .height(5.dp)
                        .cornerRadius(5.dp)
                        .background(GlanceTheme.colors.primary)
                ) {
                    // do nothing
                }
            }
        }
    }

    @Composable
    private fun BottomBar() {
        val dateText = remember { SimpleDateFormat("MM/dd HH:mm").format(System.currentTimeMillis()) }

        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(25.dp)
        ) {

            RoundedCornerBox(
                modifier = GlanceModifier.width(100.dp).fillMaxHeight(),
                disableList = listOf(Corner.TOP_RIGHT, Corner.BOTTOM_RIGHT),
                radius = 20.dp,
                contentAlignment = Alignment.Center,
                color = GlanceTheme.colors.primaryContainer
            ) {
                Row(
                    modifier = GlanceModifier.padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = Build.MODEL,
                        style = TextStyle(color = GlanceTheme.colors.primary)
                    )
                }
            }

            Spacer(modifier = GlanceModifier.width(3.dp))

            RoundedCornerBox(
                modifier = GlanceModifier.width(150.dp).fillMaxHeight(),
                disableList = listOf(Corner.TOP_LEFT, Corner.BOTTOM_LEFT),
                radius = 20.dp,
                contentAlignment = Alignment.Center,
                color = GlanceTheme.colors.primaryContainer
            ) {
                Row(modifier = GlanceModifier.padding(horizontal = 10.dp)) {
                    Text(
                        text = "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})",
                        style = TextStyle(color = GlanceTheme.colors.primary)
                    )
                }
            }
        }
    }

    enum class Corner {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_RIGHT,
        BOTTOM_LEFT
    }

    @Composable
    private fun RoundedCornerBox(
        modifier: GlanceModifier = GlanceModifier,
        color: ColorProvider = GlanceTheme.colors.primary,
        radius: Dp = 10.dp,
        disableList: List<Corner>,
        contentAlignment: Alignment,
        content: @Composable () -> Unit
    ) {
        Box(
            modifier = modifier,
            contentAlignment = contentAlignment
        ) {
            // 丸く
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(color)
                    .cornerRadius(radius),
                content = { /* do nothing */ }
            )
            // 関係ないところを隠す
            disableList.forEach { corner ->
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = when (corner) {
                        Corner.TOP_LEFT -> Alignment.TopStart
                        Corner.TOP_RIGHT -> Alignment.TopEnd
                        Corner.BOTTOM_LEFT -> Alignment.BottomStart
                        Corner.BOTTOM_RIGHT -> Alignment.BottomEnd
                    }
                ) {
                    Box(
                        modifier = modifier
                            .background(color)
                            .size(radius),
                        content = { /* do nothing */ }
                    )
                }
            }
            // 最前面に content
            content()
        }
    }

    @Composable
    private fun QsMenu(
        modifier: GlanceModifier = GlanceModifier,
        title: String,
        iconResId: Int,
        action: Action
    ) {
        Row(
            modifier = modifier
                .clickable(action)
                .padding(10.dp)
                .background(GlanceTheme.colors.primaryContainer)
                .cornerRadius(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = GlanceModifier
                    .padding(5.dp)
                    .background(GlanceTheme.colors.primary)
                    .cornerRadius(10.dp)
            ) {
                Image(
                    modifier = GlanceModifier.size(25.dp),
                    provider = ImageProvider(iconResId),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.primaryContainer)
                )
            }

            Text(
                modifier = GlanceModifier.padding(start = 5.dp),
                text = title,
                style = TextStyle(color = GlanceTheme.colors.primary)
            )

        }
    }

    /** [actionStartActivity]のアプリケーション ID 指定版 */
    private fun actionStartActivity(
        context: Context,
        applicationId: String?
    ): Action = androidx.glance.appwidget.action.actionStartActivity(
        intent = if (applicationId == null) {
            Intent()
        } else {
            context.packageManager.getLaunchIntentForPackage(applicationId)!!
        }
    )

}