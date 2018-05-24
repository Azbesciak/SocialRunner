package facebook.com.socialrunner

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import facebook.com.socialrunner.domain.data.entity.Route
import facebook.com.socialrunner.domain.service.RouteService
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.joda.time.DateTime


class NewRouteCreator(private val googleMap: GoogleMap,
                      private val apiKey: String,
                      private val onChangeListener: (List<Marker>) -> Unit,
                      private var markers: MutableList<Marker> = mutableListOf(),
                      private var startTime: DateTime = DateTime(),
                      private var canSend: Boolean = false) {
    companion object {
        const val WAYPOINT = "WAYPOINT"
    }
    var polyline: Polyline? = null

    init {
        googleMap.setOnMapClickListener { point ->
            if (!canSend) return@setOnMapClickListener
            launch(UI) {
                addPoint(point)
                drawWaypoints()
                onChangeListener(markers)
            }
        }

        googleMap.setOnMarkerClickListener { marker ->
            if (marker.tag != WAYPOINT)
                return@setOnMarkerClickListener false
            launch(UI) {
                markers.remove(marker)
                googleMap.apply {
                    marker.remove()
                    polyline?.remove()
                    drawWaypoints()
                    onChangeListener(markers)
                }
            }
            true
        }
    }

    private fun addPoint(point: LatLng) : Marker {
        val marker = point.marker()
        val m = googleMap.addMarker(marker).apply {
            tag = WAYPOINT
            setIcon(BitmapDescriptorFactory.fromResource(R.drawable.add_location))
        }
        markers.add(m)
        return m
    }

    private fun drawWaypoints() {
        if (markers.size <= 1) return
        markers.toPoints().getRouteOnMap(googleMap, apiKey, startTime){
            polyline?.remove()
            polyline = first
        }
    }

    fun send(routeService: RouteService, route: Route, userName: String) {
        routeService.uploadNewRoute(userName, route, markers.toPoints())
        googleMap.clear()
    }

    fun initialize() {
        canSend = true
        startTime = DateTime()
    }

    fun disable() {
        canSend = false
        markers.forEach{ it.remove()}
        polyline?.remove()
        markers = mutableListOf()
        polyline = null
    }

    private fun List<Marker>.toPoints() = map {it.position}.toMutableList()
}