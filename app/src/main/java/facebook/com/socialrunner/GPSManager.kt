package facebook.com.socialrunner

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log

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

        locationManager.requestLocationUpdates(locationProvider, 2000, 0f, locationListener)

    }
}
