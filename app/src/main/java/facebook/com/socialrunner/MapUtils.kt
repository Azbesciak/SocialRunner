package facebook.com.socialrunner

import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.internal.PolylineEncoding
import com.google.maps.model.DirectionsResult
import com.google.maps.model.DirectionsRoute
import com.google.maps.model.TravelMode
import facebook.com.socialrunner.domain.data.entity.Location
import facebook.com.socialrunner.domain.data.entity.Route
import facebook.com.socialrunner.domain.data.entity.RoutePoint
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.joda.time.DateTime
import java.util.concurrent.TimeUnit


typealias LL = com.google.maps.model.LatLng
const val overview = 0

fun GoogleMap.addMarkersToMap(results: DirectionsResult) {
    with(results.first()) {
        addMarker(MarkerOptions().position(LatLng(startLocation.lat, startLocation.lng)))
        addMarker(MarkerOptions().position(LatLng(endLocation.lat, endLocation.lng))
                .title(startAddress).snippet(getEndLocationTitle(results)))
    }
}

fun GoogleMap.positionCamera(route: DirectionsRoute, zoom : Float) {
    with(route.legs[overview].startLocation) {
        moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), zoom))
    }
}

fun GoogleMap.createPolyline(results: DirectionsResult): Polyline? {
    val decodedPath = PolylineEncoding
            .decode(results.routes[overview].overviewPolyline.encodedPath)
            .map { LatLng(it.lat, it.lng) }
    return addPolyline(PolylineOptions().addAll(decodedPath))
}

fun getEndLocationTitle(results: DirectionsResult) = with(results.first()) {
    "Time :${duration.humanReadable} Distance :${distance.humanReadable}"
}

fun DirectionsResult.first() = routes[overview].legs[overview]

fun List<LatLng>.getDirectionsDetails(mode: TravelMode, departure: DateTime, apiKey: String) =
        try {
            val wp = map { LL(it.latitude, it.longitude) }
            val centerWp = wp.drop(1).dropLast(1).toTypedArray()
            DirectionsApi.newRequest(getGeoContext(apiKey))
                    .mode(mode)
                    .origin(wp.first())
                    .destination(wp.last())
                    .waypoints(*centerWp)
                    .departureTime(departure)
                    .await()
        } catch (e: Throwable) {
            Log.e("DIRECTION EXC", "error while download directions", e)
            null
        }

fun getGeoContext(apiKey: String): GeoApiContext = GeoApiContext.Builder().apiKey(apiKey)
        .queryRateLimit(3)
        .readTimeout(1, TimeUnit.SECONDS)
        .writeTimeout(1, TimeUnit.SECONDS)
        .build()

fun List<LatLng>.toRoutePoints() =
        map { RoutePoint(loc = Location(it.latitude, it.longitude)) }.toMutableList()

fun Route.toWayPoints() = routePoints.map { LatLng(it.loc.latitude, it.loc.longitude) }

fun List<LatLng>.getRouteOnMap(map: GoogleMap, apiKey: String, startTime: DateTime = DateTime.now()) =
        getDirectionsDetails(TravelMode.WALKING, startTime, apiKey)?.let { results ->
            launch(UI) {
                with(map) {
                    createPolyline(results)
                    addMarkersToMap(results)
                }

            }
            results
        }
