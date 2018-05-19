package facebook.com.socialrunner

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

class NewRouteCreator(val googleMap: GoogleMap, val onRouteChangeListener: (List<LatLng>) -> Unit,
                      var waypoints: MutableList<LatLng> = mutableListOf(),
                      var startTime: Date = Date() , private var canSend: Boolean = false) {
    init {
        googleMap.setOnMapClickListener { point ->
            if (!canSend) return@setOnMapClickListener
            val marker = MarkerOptions().position(point)
            googleMap.addMarker(marker)
            waypoints.add(point)
            onRouteChangeListener(waypoints)
        }
    }

    fun send() {
        googleMap.clear()
    }

    fun initialize() {
        canSend = true
        startTime = Date()
    }

    fun disable() {
        canSend = false
        waypoints = mutableListOf()
        googleMap.clear()
    }
}