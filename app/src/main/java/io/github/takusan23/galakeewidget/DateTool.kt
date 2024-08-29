package io.github.takusan23.galakeewidget

import java.text.SimpleDateFormat
import java.util.Locale

object DateTool {

    fun createDateData(): DateData {
        val locale = Locale("ja", "JP", "JP")
        // 元号を表示して欲しい
        val eraFormat = android.icu.text.SimpleDateFormat("GGGGy年", locale)
        val nowEra = eraFormat.format(System.currentTimeMillis())

        // それ以外
        val dateFormat = SimpleDateFormat("yyyy年($nowEra)\nM月d日(E曜日)", locale)
        val timeFormat = SimpleDateFormat("HH:mm", locale)
        val calender = Runtime.getRuntime().exec(arrayOf("cal")).inputStream.bufferedReader().use { bufferedReader ->
            bufferedReader.readText()
        }

        return DateData(
            date = dateFormat.format(System.currentTimeMillis()),
            time = timeFormat.format(System.currentTimeMillis()),
            calender = calender
        )
    }

    data class DateData(
        val date: String,
        val time: String,
        val calender: String
    )

}