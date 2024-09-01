package io.github.takusan23.galakeewidget

import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.datastore.preferences.core.edit
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
                it.saveNotificationIconList(emptyArray())
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        saveNotificationIcons()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        saveNotificationIcons()
    }

    private fun saveNotificationIcons() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            scope.launch {
                this@GalakeeWidgetNotificationListener.dataStore.edit {
                    it.saveNotificationIconList(activeNotifications)
                }
            }
        }
    }

}