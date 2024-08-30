package io.github.takusan23.galakeewidget

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import org.json.JSONArray

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object DataStore {

    /** 待ち受け画面の壁紙 Uri */
    private val STANDBY_WALLPAPER = stringPreferencesKey("standby_wallpaper")

    fun Preferences.getWallpaperUriList(): List<Uri> {
        val readJson = this[STANDBY_WALLPAPER] ?: return emptyList()
        val jsonArray = JSONArray(readJson)
        return (0 until jsonArray.length())
            .map { index -> jsonArray.getString(index) }
            .map { it.toUri() }
    }

    fun MutablePreferences.saveWallpaperUriList(uriList: List<Uri>) {
        val jsonArray = JSONArray().apply {
            uriList.forEach { uri -> put(uri.toString()) }
        }
        this[STANDBY_WALLPAPER] = jsonArray.toString()
    }

}