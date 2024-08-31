package io.github.takusan23.galakeewidget.tool

import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object BarometerWeatherTool {

    enum class Weather {
        SUN,
        CLOUDY,
        RAIN
    }

    suspend fun getBarometerWeather(context: Context) = suspendCancellableCoroutine {
        val sensorManager = context.getSystemService(SENSOR_SERVICE) as SensorManager
        val sensorList: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_PRESSURE)
        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event ?: return
                val barometer = Math.round(event.values[0])
                val weather = when {
                    1013 < barometer -> Weather.SUN
                    1000 < barometer -> Weather.CLOUDY
                    else -> Weather.RAIN
                }
                // サスペンド関数の返り値
                it.resume(weather)
                //登録解除
                sensorManager.unregisterListener(this)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // do nothing
            }
        }
        sensorManager.registerListener(sensorEventListener, sensorList[0], SensorManager.SENSOR_DELAY_NORMAL)
        it.invokeOnCancellation { sensorManager.unregisterListener(sensorEventListener) }
    }

}