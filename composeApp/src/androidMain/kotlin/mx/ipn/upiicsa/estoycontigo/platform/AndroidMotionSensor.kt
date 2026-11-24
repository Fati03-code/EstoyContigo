package mx.ipn.upiicsa.estoycontigo.platform

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlin.math.sqrt

class AndroidMotionSensor(context: Context) : MotionSensor {
    private val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    override val isAvailable: Boolean get() = accelerometer != null

    override fun shakes(): Flow<Unit> {
        val sensor = accelerometer ?: return emptyFlow()
        return callbackFlow {
            val detector = ShakeDetector { trySend(Unit) }
            val listener = object : SensorEventListener {
                override fun onSensorChanged(e: SensorEvent) {
                    detector.onSample(e.values[0], e.values[1], e.values[2], e.timestamp / 1_000_000)
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
            awaitClose { manager.unregisterListener(listener) }
        }
    }

    override fun movement(): Flow<Float> {
        val sensor = accelerometer ?: return emptyFlow()
        return callbackFlow {
            var smoothed = 0f
            val gravity = FloatArray(3)
            val listener = object : SensorEventListener {
                override fun onSensorChanged(e: SensorEvent) {
                    for (i in 0..2) gravity[i] = 0.9f * gravity[i] + 0.1f * e.values[i]
                    val x = e.values[0] - gravity[0]
                    val y = e.values[1] - gravity[1]
                    val z = e.values[2] - gravity[2]
                    smoothed = 0.85f * smoothed + 0.15f * sqrt(x * x + y * y + z * z)
                    trySend(smoothed)
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
            awaitClose { manager.unregisterListener(listener) }
        }
    }
}
