package io.github.takusan23.galakeewidget.tool

import android.content.Context
import android.graphics.drawable.Icon
import android.net.Uri
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import org.json.JSONArray
import org.json.JSONObject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object DataStore {

    /** 待ち受け画面の壁紙 Uri */
    private val STANDBY_WALLPAPER = stringPreferencesKey("standby_wallpaper")

    /** ショートカット */
    private val SHORTCUT_APP_ID_ONE = stringPreferencesKey("shortcut_appid_one")
    private val SHORTCUT_APP_ID_TWO = stringPreferencesKey("shortcut_appid_two")
    private val SHORTCUT_APP_ID_THREE = stringPreferencesKey("shortcut_appid_three")

    /** 通知 */
    private val NOTIFICATION_LIST = stringPreferencesKey("notification_list")

    /** 複数の Uri を読み出す */
    fun Preferences.getWallpaperUriList(): List<Uri> {
        val readJson = this[STANDBY_WALLPAPER] ?: return emptyList()
        val jsonArray = JSONArray(readJson)
        return (0 until jsonArray.length())
            .map { index -> jsonArray.getString(index) }
            .map { it.toUri() }
    }

    /** 複数の Uri を保存する */
    fun MutablePreferences.saveWallpaperUriList(uriList: List<Uri>) {
        val jsonArray = JSONArray().apply {
            uriList.forEach { uri -> put(uri.toString()) }
        }
        this[STANDBY_WALLPAPER] = jsonArray.toString()
    }

    /** ショートカットで起動するアプリ取得 */
    fun Preferences.getShortcutAppIdData(): ShortcutAppIdData {
        return ShortcutAppIdData(
            one = this[SHORTCUT_APP_ID_ONE],
            two = this[SHORTCUT_APP_ID_TWO],
            three = this[SHORTCUT_APP_ID_THREE]
        )
    }

    /** ショートカットで起動するアプリを保存 */
    fun MutablePreferences.saveShortcutAppIdData(shortcutAppIdData: ShortcutAppIdData) {
        shortcutAppIdData.one?.also { this[SHORTCUT_APP_ID_ONE] = it }
        shortcutAppIdData.two?.also { this[SHORTCUT_APP_ID_TWO] = it }
        shortcutAppIdData.three?.also { this[SHORTCUT_APP_ID_THREE] = it }
    }

    fun Preferences.getNotificationIconList(context: Context): List<Icon> {
        val readJson = this[NOTIFICATION_LIST] ?: return emptyList()
        val jsonArray = JSONArray(readJson)

        return (0 until jsonArray.length())
            .map { jsonArray.getJSONObject(it) }
            .mapNotNull { json ->
                val applicationId = json.getString("application_id")
                val resId = json.getInt("res_id")
                // val applicationInfo = context.packageManager.getApplicationInfo(applicationId, PackageManager.MATCH_UNINSTALLED_PACKAGES or PackageManager.GET_SHARED_LIBRARY_FILES)
                // ResourcesCompat.getDrawable(context.packageManager.getResourcesForApplication(applicationInfo), resId, context.theme)
                Icon.createWithResource(applicationId, resId)
            }
    }

    fun MutablePreferences.saveNotificationIconList(dataList: List<NotificationIconData>) {
        val jsonArray = JSONArray().apply {
            dataList.forEach { data ->
                put(JSONObject().apply {
                    put("application_id", data.applicationId)
                    put("res_id", data.resId)
                })
            }
        }
        this[NOTIFICATION_LIST] = jsonArray.toString()
    }

    data class NotificationIconData(
        val applicationId: String,
        val resId: Int
    )

    data class ShortcutAppIdData(
        val one: String? = null,
        val two: String? = null,
        val three: String? = null
    )

}