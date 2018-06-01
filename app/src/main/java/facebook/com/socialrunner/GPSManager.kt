package facebook.com.socialrunner

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import facebook.com.socialrunner.domain.data.entity.Position

const val locationProvider = LocationManager.GPS_PROVIDER

class GPSManager(val newPositionCallback: (Location) -> Unit) {

    @SuppressLint("MissingPermission")
    fun getPosition(mapsActivity: MapsActivity) {
        // Acquire a reference to the system Location Manager
        val locationManager = mapsActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Define a listener that responds to location updates
        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                newPositionCallback(location)
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

            override fun onProviderEnabled(provider: String) {}

            override fun onProviderDisabled(provider: String) {}
        }

        locationManager.requestLocationUpdates(locationProvider, 10000, 50f, locationListener)
    }

    companion object {
        fun calculateDistance(latitude1 : Double, latitude2 : Double, longitude1: Double, longitude2: Double) : Double
        {
            val R = 6378.137
            val dLat = latitude1 * Math.PI / 180 - latitude2 * Math.PI / 180
            val dLon = longitude1 * Math.PI / 180 - longitude2 * Math.PI / 180
            val temp = Math.sin(dLat/2) * Math.sin(dLat / 2) +
                    Math.cos(latitude1 / 180) * Math.cos(latitude2 / 180) *
                    Math.sin(dLon/2) * Math.sin(dLon / 2)
            val temp2 = 2 * Math.atan2(Math.sqrt(temp), Math.sqrt(1-temp))
            return R * temp2 * 1000;
        }
        fun calculateDistance(position1 : Location, position2 : Location) =
                calculateDistance(position1.latitude, position2.latitude, position1.longitude, position2.longitude)
        fun calculateDistance(position1 : Position, position2 : Position) =
                calculateDistance(position1.latitude, position2.latitude, position1.longitude, position2.longitude)
    }
}
