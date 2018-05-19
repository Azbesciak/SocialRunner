package facebook.com.socialrunner

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.internal.PolylineEncoding
import com.google.maps.model.DirectionsResult
import com.google.maps.model.DirectionsRoute
import com.google.maps.model.TravelMode
import org.joda.time.DateTime
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.math.abs


class NewRouteCreator(val googleMap: GoogleMap, val apiKey: String, val onRouteChangeListener: (List<LatLng>) -> Unit,
                      var waypoints: MutableList<LatLng> = mutableListOf(),
                      var startTime: DateTime = DateTime(), private var canSend: Boolean = false) {
    companion object {
        const val overview = 0
        const val epsilon = 0.001
    }

    init {
        googleMap.setOnMapClickListener { point ->
            if (!canSend) return@setOnMapClickListener
            val marker = point.marker()
            googleMap.addMarker(marker)
            waypoints.add(point)
            onRouteChangeListener(waypoints)
            if (waypoints.size <= 1) return@setOnMapClickListener
            drawWaypoints()
        }

        googleMap.setOnMarkerClickListener { marker ->
            with(marker.position) {
                waypoints.removeIf {l ->
                    abs(l.latitude - latitude) < epsilon && abs(l.longitude - longitude) < epsilon
                }
            }
            onRouteChangeListener(waypoints)
            googleMap.apply {
                clear()
                if (waypoints.size < 2) {
                    waypoints.forEach { addMarker(it.marker()) }
                } else {
                    drawWaypoints()
                }
            }
            false
        }
    }

    private fun LatLng.marker() = MarkerOptions().position(this)!!

    private fun drawWaypoints() {
        getDirectionsDetails(TravelMode.WALKING)?.let {results ->
            addPolyline(results, googleMap)
            positionCamera(results.routes[overview], googleMap)
            addMarkersToMap(results, googleMap)
        }
    }

    fun send() {
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

    private fun addMarkersToMap(results: DirectionsResult, mMap: GoogleMap) {
        mMap.addMarker(MarkerOptions().position(LatLng(
                results.routes[overview].legs[overview].startLocation.lat,
                results.routes[overview].legs[overview].startLocation.lng)))
        mMap.addMarker(MarkerOptions().position(
                LatLng(results.routes[overview].legs[overview].endLocation.lat,
                        results.routes[overview].legs[overview].endLocation.lng))
                .title(results.routes[overview].legs[overview].startAddress).snippet(getEndLocationTitle(results)))
    }

    private fun positionCamera(route: DirectionsRoute, mMap: GoogleMap) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(route.legs[overview].startLocation.lat, route.legs[overview].startLocation.lng), 12f))
    }

    private fun addPolyline(results: DirectionsResult, mMap: GoogleMap) {
        val decodedPath = PolylineEncoding
                .decode(results.routes[overview].overviewPolyline.encodedPath)
                .map { LatLng(it.lat, it.lng) }
        mMap.addPolyline(PolylineOptions().addAll(decodedPath))
    }

    private fun getEndLocationTitle(results: DirectionsResult): String {
        return "Time :" + results.routes[overview].legs[overview].duration.humanReadable + " Distance :" + results.routes[overview].legs[overview].distance.humanReadable
    }

    private fun getDirectionsDetails(mode: TravelMode): DirectionsResult? {
        try {
            val wp = waypoints.map { LL(it.latitude, it.longitude) }
            val centerWp = wp.drop(1).dropLast(1).toTypedArray()
            return DirectionsApi.newRequest(getGeoContext())
                    .mode(mode)
                    .origin(wp.first())
                    .destination(wp.last())
                    .waypoints(*centerWp)
                    .departureTime(startTime)
                    .await()
        } catch (e: ApiException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun getGeoContext(): GeoApiContext {
        return GeoApiContext.Builder().apiKey(apiKey)
                .queryRateLimit(3)
                .readTimeout(1, TimeUnit.SECONDS)
                .writeTimeout(1, TimeUnit.SECONDS)
                .build()
    }

}
typealias LL = com.google.maps.model.LatLng
