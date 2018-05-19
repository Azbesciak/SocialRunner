package facebook.com.socialrunner

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class NewRouteCreator(val googleMap: GoogleMap, var waypoints: MutableList<LatLng> = mutableListOf(), var startTime: Long = 0, private var canSend: Boolean = false) {
    init {
        googleMap.setOnMapClickListener { point ->
            val marker = MarkerOptions().position(point)
            googleMap.addMarker(marker)
            waypoints.add(point)
        }
    }

    fun send() {
        googleMap.clear()
    }

    fun initialize() {
        canSend = true
    }
    fun disable() {
        canSend = false
        waypoints = mutableListOf()
        googleMap.clear()
    }
}