package facebook.com.socialrunner

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.model.TravelMode
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
    }

    init {
        googleMap.setOnMapClickListener { point ->
            launch(UI) {
                if (!canSend) return@launch
                addPoint(point)
                onRouteChangeListener(waypoints)
                drawWaypoints()
            }

        }

        googleMap.setOnMarkerClickListener { marker ->
            launch(UI) {
                with(marker.position) {
                    waypoints.removeIf {
                        abs(it.latitude - latitude) < epsilon &&
                                abs(it.longitude - longitude) < epsilon
                    }
                }
                onRouteChangeListener(waypoints)
                googleMap.apply {
                    clear()
                    waypoints.forEach { addMarker(it.marker()) }
                    drawWaypoints()
                }
            }
            true
        }
    }

    fun addPoint(point: LatLng) {
        val marker = point.marker()
        googleMap.addMarker(marker)
        waypoints.add(point)
    }

    fun removeMarker(point : LatLng)
    {
        if(waypoints.contains(point))
            waypoints.remove(point)
    }
    private fun LatLng.marker() = MarkerOptions().position(this)!!

    private fun drawWaypoints() {
        if (waypoints.size <= 1) return
        waypoints.getRouteOnMap(googleMap, apiKey, startTime)?.let { results ->
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