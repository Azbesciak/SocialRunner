package facebook.com.socialrunner

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import facebook.com.socialrunner.domain.data.entity.Position
import facebook.com.socialrunner.domain.data.entity.Route
import facebook.com.socialrunner.domain.data.entity.RoutePoint
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

object MockRunnerService {
    private val runners = mutableListOf("Janek", "Krysia", "Heniu", "Przemek", "Andrzej").map { Runner(it) }
    @Synchronized
    fun MapsActivity.running(route: Route) {
        runners.firstOrNull { !it.isRunning }?. let {
            it.isRunning = true
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
class MockRunner(var mapActivity: MapsActivity,
                 private val runner: Runner,
                 private val waypoint: List<RoutePoint>,
                 private var lastMarker: MarkerOptions = MarkerOptions(),
                 private var pos: Int = 0
) {
    fun run() {
        var positionSet = false
        launch {
            while (pos < waypoint.size - 1) {
                var coef = 0.0f
                while (coef < 1.0001f) {
                    val tempPos = Position(waypoint[pos].loc.latitude + coef * (waypoint[pos + 1].loc.latitude - waypoint[pos].loc.latitude),
                            waypoint[pos].loc.longitude + coef * (waypoint[pos + 1].loc.longitude - waypoint[pos].loc.longitude))
                    Log.i("pos2", "New position of ${runner.name} is ${tempPos.longitude} ${tempPos.latitude}")

                    val pos = LatLng(tempPos.latitude, tempPos.longitude)
                    launch(UI) {
                        if (!positionSet) {
                            positionSet = true
                            lastMarker = mapActivity.placeMarkerOnMap(pos)
                        } else {
                            lastMarker.position(pos)
                        }
                    }
                    coef += 0.05f
                    delay(2000)
                }
                pos += 1
            }
            runner.isRunning = false
        }

    }

}