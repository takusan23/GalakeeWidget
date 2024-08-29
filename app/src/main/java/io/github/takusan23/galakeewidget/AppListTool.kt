package io.github.takusan23.galakeewidget

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

object AppListTool {

    /**
     * アプリの使用状況を問い合わせ、利用頻度が高いアプリ一覧を取得する
     *
     * @param context [context]
     * @param timeMachineDateCount 何日前まで遡って利用状況を問い合わせるか。負の値である必要があります。
     * @return [AppInfoData]の配列
     */
    // TODO この関数重いかも
    suspend fun querySuggestAppList(
        context: Context,
        timeMachineDateCount: Int = -1
    ): List<AppInfoData> = withContext(Dispatchers.IO) {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val packageManager = context.packageManager
        // さかのぼる日数
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, timeMachineDateCount)
        // クエリする
        val statusDataList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, calendar.timeInMillis, System.currentTimeMillis())
        // 同じアプリケーションIDの UsageStats が複数回含まれるため、一つにまとめて足す処理を書きます
        val statusDataHashMap = hashMapOf<String, UsageStats>()
        statusDataList.forEach { usageStats ->
            statusDataHashMap[usageStats.packageName] = statusDataHashMap[usageStats.packageName]?.apply { add(usageStats) } ?: usageStats
        }
        // アプリ情報を追加で取得して返す
        statusDataHashMap
            // 一回も起動してないやつは除外
            .filter { (_, usageStats) -> usageStats.totalTimeInForeground > 0 }
            // アプリケーション ID からアプリ情報を取得し内包して返す
            .mapNotNull { (packageName, usageStats) ->
                val applicationInfo = runCatching { packageManager.getApplicationInfo(packageName, 0) }.getOrNull() ?: return@mapNotNull null
                val intent = packageManager.getLaunchIntentForPackage(packageName) ?: return@mapNotNull null

                val iconBitmap = applicationInfo.loadIcon(packageManager).toBitmap()
                val appLabel = packageManager.getApplicationLabel(applicationInfo).toString()
                AppInfoData(
                    packageName = packageName,
                    label = appLabel,
                    icon = iconBitmap,
                    intent = intent,
                    foregroundUsageTimeMs = usageStats.totalTimeInForeground
                )
            }
            // 使う時間が長い順
            .sortedByDescending { it.foregroundUsageTimeMs }
    }


    /**
     * アプリのアイコンと名前と起動インテント
     *
     * @param packageName パッケージ名
     * @param label アプリ名
     * @param icon アイコン
     * @param intent アプリ起動インテント
     * @param foregroundUsageTimeMs 使用時間
     */
    data class AppInfoData(
        val packageName: String,
        val label: String,
        val icon: Bitmap,
        val intent: Intent,
        val foregroundUsageTimeMs: Long
    )
}