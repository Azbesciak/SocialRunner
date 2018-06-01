package facebook.com.socialrunner

import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

class PermissionsGuard(var activity : Activity){
    fun acquirePermissions() {
        checkPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE, STORAGE_PERMISSIONS_REQUEST_CODE)
        checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION, LOCATION_PERMISSION_REQUEST_CODE)
    }

    private fun checkPermission(permission : String, permissionCode : Int){
        if (ContextCompat.checkSelfPermission(activity,permission) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                ActivityCompat.requestPermissions(activity,
                        arrayOf(permission), permissionCode)
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity, arrayOf(permission),
                        permissionCode)
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val STORAGE_PERMISSIONS_REQUEST_CODE = 2
    }
}

