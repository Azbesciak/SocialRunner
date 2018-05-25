package facebook.com.socialrunner

import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

class PermissionsGuard(var activity : Activity){
    fun accuirePermisions()
    {
        checkPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)

    }

    private fun checkPermission(permission : String){
        if (ContextCompat.checkSelfPermission(activity,permission) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                ActivityCompat.requestPermissions(activity,
                        arrayOf(permission), LOCATION_PERMISSION_REQUEST_CODE)
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity, arrayOf(permission),
                        LOCATION_PERMISSION_REQUEST_CODE)
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val STORAGE_PERMISSIONS_REQUEST_CODE = 2
    }
}

