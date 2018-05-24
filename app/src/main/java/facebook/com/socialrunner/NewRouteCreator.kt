package facebook.com.socialrunner

import android.os.Build
import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import facebook.com.socialrunner.domain.data.entity.Route
import facebook.com.socialrunner.domain.service.RouteService
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.joda.time.DateTime
import kotlin.math.abs


class NewRouteCreator(val googleMap: GoogleMap, val apiKey: String, val onRouteChangeListener: (List<LatLng>) -> Unit,
                      var waypoints: MutableList<LatLng> = mutableListOf(),
                      var startTime: DateTime = DateTime(), private var canSend: Boolean = false) {
    companion object {
        const val epsilon = 0.0005
        const val LOCATION = "LOC"
        const val RUNNER = "RUN"
    }
    var polyline: Polyline? = null

    init {

        googleMap.setOnMapClickListener { point ->
            if (!canSend) return@setOnMapClickListener
            launch(UI) {
                addPoint(point, true)
                onRouteChangeListener(waypoints)
                drawWaypoints()
            }
        }

        googleMap.setOnMarkerClickListener { marker ->
            if (marker.tag == RUNNER)
                return@setOnMarkerClickListener false
            launch(UI) {
                with(marker.position) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        waypoints.removeIf {
                            abs(it.latitude - latitude) < epsilon &&
                                    abs(it.longitude - longitude) < epsilon
                        }
                    }
                }
                googleMap.apply {
//                    clear()
                    marker.remove()
                    polyline?.remove()
//                    waypoints.forEach { addMarker(it.marker()) }
                    drawWaypoints()
                }
//                onRouteChangeListener(waypoints)
            }
            true
        }
    }

    fun addPoint(point: LatLng, isLocation: Boolean, f: (MarkerOptions) -> Unit = {}) : Marker {
        val marker = point.marker()
        f(marker)
        val m = googleMap.addMarker(marker).apply {
            tag = if (isLocation) LOCATION else RUNNER
        }
        waypoints.add(point)
        return m
    }

    private fun LatLng.marker() = MarkerOptions().position(this)!!

    private fun drawWaypoints() {
        if (waypoints.size <= 1) return
        waypoints.getRouteOnMap(googleMap, apiKey, startTime){
            polyline?.remove()
            polyline = first
        }?.let { results ->
            with(googleMap) {
                addMarkersToMap(results)
                positionCamera(results.routes[overview], googleMap.cameraPosition.zoom)
            }
        }
    }

    fun send(routeService: RouteService, route: Route, userName: String) {
        routeService.uploadNewRoute(userName, route, waypoints.toMutableList())
        googleMap.clear()
    }

    fun initialize() {
        canSend = true
        startTime = DateTime()
    }

    fun disable() {
        canSend = false
        waypoints = mutableListOf()
        googleMap.clear()
    }
}