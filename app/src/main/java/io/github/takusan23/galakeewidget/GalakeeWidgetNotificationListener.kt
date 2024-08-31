package io.github.takusan23.galakeewidget

import android.graphics.drawable.Icon
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.datastore.preferences.core.edit
import io.github.takusan23.galakeewidget.tool.DataStore
import io.github.takusan23.galakeewidget.tool.DataStore.saveNotificationIconList
import io.github.takusan23.galakeewidget.tool.dataStore
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class GalakeeWidgetNotificationListener : NotificationListenerService() {

    private val scope = MainScope()

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        // 切断時は DataStore からも消す
        scope.launch {
            this@GalakeeWidgetNotificationListener.dataStore.edit {
                it.saveNotificationIconList(emptyList())
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        saveNotificationIcons(sbn = sbn ?: return)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        saveNotificationIcons(sbn = sbn ?: return)
    }

    private fun saveNotificationIcons(sbn: StatusBarNotification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // TYPE_RESOURCE のみ。JSON でシリアライズして DataStore にいれる。
            // 上位数件のみ
            val notificationIconList = activeNotifications
                .filter { it.notification.smallIcon.type == Icon.TYPE_RESOURCE }
                .distinctBy { it.packageName }
                .take(5)
                .map { notification ->
                    DataStore.NotificationIconData(
                        notification.packageName,
                        notification.notification.smallIcon.resId
                    )
                }
            scope.launch {
                this@GalakeeWidgetNotificationListener.dataStore.edit {
                    it.saveNotificationIconList(notificationIconList)
                }
            }
        }
    }

}