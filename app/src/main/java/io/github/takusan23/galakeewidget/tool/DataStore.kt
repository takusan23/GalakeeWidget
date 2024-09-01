package io.github.takusan23.galakeewidget.tool

import android.app.Notification
import android.content.Context
import android.net.Uri
import android.service.notification.StatusBarNotification
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
    private val NOTIFICATION_DATA = stringPreferencesKey("notification_data2")

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

    fun Preferences.getNotificationIconList(): NotificationData {
        val readJson = this[NOTIFICATION_DATA] ?: return NotificationData()
        val jsonObject = JSONObject(readJson)

        return NotificationData(
            notificationCount = jsonObject.getInt("count"),
            hasConversation = jsonObject.getBoolean("has_conversation")
        )
    }

    fun MutablePreferences.saveNotificationIconList(statusBarNotificationList: Array<StatusBarNotification>) {
        val jsonObject = JSONObject().apply {
            put("count", statusBarNotificationList.count { it.isClearable }) // 削除可能なものだけ
            put("has_conversation", statusBarNotificationList.any { it.notification.extras.containsKey(Notification.EXTRA_IS_GROUP_CONVERSATION) }) // 会話かどうか
        }
        this[NOTIFICATION_DATA] = jsonObject.toString()
    }

    data class NotificationData(
        val notificationCount: Int = 0,
        val hasConversation: Boolean = false
    )

    data class ShortcutAppIdData(
        val one: String? = null,
        val two: String? = null,
        val three: String? = null
    )

}