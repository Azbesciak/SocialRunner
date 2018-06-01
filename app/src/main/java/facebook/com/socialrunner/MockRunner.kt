package facebook.com.socialrunner

import android.util.Log
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import facebook.com.socialrunner.domain.data.entity.Position
import facebook.com.socialrunner.domain.data.entity.Route
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.util.*

object MockRunnerService {
    val runners = mutableListOf("Janek", "Krysia", "Heniu", "Przemek", "Andrzej")
            .map { Runner(it) }
    @Synchronized
    fun MapsActivity.running(route: Route) {
        runners.firstOrNull { !it.isRunning}?. let {
            val runner = MockRunner(this, it, route.routePoints)
            launch {
                Log.i("Mock Runner", "${it.name} started!")
                delay(3000)
                runner.run()
            }
        }
    }
}
data class Runner(val name: String, var isRunning: Boolean = false)
class MockRunner(private var mapActivity: MapsActivity,
                 private val runner: Runner,
                 private val waypoint: List<Position>,
                 private var pos: Int = 0
) {
    private lateinit var lastMarker: Marker
    val minVelocity = 15 //1.38
    val maxVelocity = 20 //4.72
    fun run() {
        var runningCoefficient: Double
        val delayMs = 200//ms
        val targetVelocity = Math.max(minVelocity + (maxVelocity-minVelocity)*Random().nextDouble(), 3.0)   //1.38m/s = 5km/h, 4.72m/s = 17km/h
        launch {
            while (pos < waypoint.size - 1) {
                var progress = 0.0
                val locA = waypoint[pos]
                val locB = waypoint[pos + 1]
                val distance = GPSManager.calculateDistance(locA, locB)
                runningCoefficient = ((delayMs/1000.0)*targetVelocity)/distance
                while (progress < 1.0001) {
                    val tempPos = Position(locA.latitude + progress * (locB.latitude - locA.latitude),
                            locA.longitude + progress * (locB.longitude - locA.longitude))
                   // Log.i("pos2", "New position of ${runner.name} is ${tempPos.longitude} ${tempPos.latitude}")

                    val pos = LatLng(tempPos.latitude, tempPos.longitude)
                    launch(UI) {
                        if (!::lastMarker.isInitialized) {
                            lastMarker = mapActivity.addMarker(pos).apply {
                                setIcon(BitmapDescriptorFactory.fromResource(R.drawable.runner))
                            }
                            runner.isRunning = true
                        } else {
                            lastMarker.position = pos
                        }
                    }
                    progress += runningCoefficient
                    delay(delayMs)
                }
                pos += 1
            }
            if (::lastMarker.isInitialized) {
                launch(UI){
                    lastMarker.remove()
                }
            }
            runner.isRunning = false
        }
    }
}