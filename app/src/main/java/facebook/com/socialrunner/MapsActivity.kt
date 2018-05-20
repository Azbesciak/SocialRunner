package facebook.com.socialrunner

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.firebase.FirebaseApp
import facebook.com.socialrunner.domain.data.entity.Position
import facebook.com.socialrunner.domain.data.entity.Route
import facebook.com.socialrunner.domain.data.localdata.User
import facebook.com.socialrunner.domain.service.RouteService
import facebook.com.socialrunner.domain.service.RunnerService
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.io.IOException
import java.util.*
import java.util.jar.Manifest
import kotlin.math.abs

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var _username: String
    private var username : String
        get() = _username
        set(value) {
            _username = value
        }
    private lateinit var locationRequest: LocationRequest
    private lateinit var localStorage : LocalStorageManager
    private var locationUpdateState = false
    private val routeService by lazy { RouteService() }
    private val runnerService by lazy { RunnerService() }
    private val apiKey by lazy {getString(R.string.google_api_key) }

    private val routeCreator by lazy {
        NewRouteCreator(map, apiKey,
                { sendRouteBtn.isEnabled = it.isNotEmpty() }
        )
    }
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val gpsManager = GPSManager(::newPosition)


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
        private const val PLACE_PICKER_REQUEST = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 2)


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        initButtons()
        createLocationRequest()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        FirebaseApp.initializeApp(this)
        localStorage = LocalStorageManager("user_data", applicationContext)
    }

    private fun initButtons() {
        stopAdding()
        createRouteBtn.setOnClickListener {
            startAdding()
        }

        sendRouteBtn.setOnClickListener {
            sendRouteBtn.isEnabled = false
            stopAdding()
            NewRunDialog().setContext(applicationContext)
                    .setCallbacks(::startRun, ::postponeRun)
                    .show(fragmentManager, "some not important tag")
        }

        cancelRouteBtn.setOnClickListener {
            stopAdding()
        }
    }

    private fun startRun(pace : Double)
    {
        this.pace = pace
        val route = Route()
        val c = Calendar.getInstance()
        route.startHour = c.get(Calendar.HOUR_OF_DAY)
        route.startMinute = c.get(Calendar.MINUTE)
        route.pace = pace
        Log.i("run", "pace is $pace")
        localStorage.saveUser(User(username, 0.0))
        routeCreator.send(routeService, route ,username)
    }
    private var pace = 0.0
    private fun postponeRun(pace : Double)
    {
        this.pace = pace
        Log.i("run", "pace is $pace")
        localStorage.saveUser(User(username, 0.0))
        TimePickerFragment().setCallback(::runTimePicked).show(fragmentManager, "this tag is awesome!")
    }

    private fun runTimePicked(hour : Int, minute : Int)
    {
        Log.i("run", "run time is set to $hour:$minute")
        val route = Route(startHour = hour, startMinute = minute, pace = pace)
        routeCreator.send(routeService, route ,username)
    }

    private fun startAdding() {
        sendRouteBtn.isEnabled = false
        routeCreator.initialize()
        sendRouteBtn.show()
        cancelRouteBtn.show()
        createRouteBtn.hide()
    }

    private fun stopAdding() {
        if (::map.isInitialized) {
            routeCreator.disable()
        }
        sendRouteBtn.hide()
        cancelRouteBtn.hide()
        createRouteBtn.show()
    }

    override fun onStart() {
        super.onStart()
        val user = localStorage.loadUser()
        if(user == null)
        {
            val account = GoogleSignIn.getLastSignedInAccount(applicationContext)
            Log.i(auth, "$account")
            if (account == null) {
                signIn()
            } else {
                username = account.email?.split("@")?.get(0) ?: "unknown_username"
                Log.i(auth, "saved authenticated user is $username")
                localStorage.saveUser(User(username, 0.0))
            }
        }else{
            username = user.name!!
        }


        gpsManager.getPosition(this)
    }

    private val RC_SIGN_IN = 1000
    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun newPosition(location: Location) {
        Log.i("gps", "New position in main lat:${location.latitude}, lon:${location.longitude}")
        runnerService.updateRunnerLocation(username, Position(latitude = location.latitude, longitude = location.longitude))
    }

    private val auth = "auth"
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                val place = PlacePicker.getPlace(this, data)
                var addressText = place.name.toString()
                addressText += "\n" + place.address.toString()

                placeMarkerOnMap(place.latLng)
            }
        }

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data);
            val account: GoogleSignInAccount?
            try {
                account = task.getResult(ApiException::class.java)
            } catch (e : ApiException) {
                // The ApiException status code indicates the detailed failure reason.
                // Please refer to the GoogleSignInStatusCodes class reference for more information.
                Log.w(auth, "signInResult:failed code=" + e.statusCode)
                Toast.makeText(applicationContext, "Something went wrong, choosing random username.", LENGTH_SHORT).show()
                username = "user_${abs(Random().nextInt() % 1000000)}"
                localStorage.saveUser(User(username, 0.0))
                return
            }
            account?.let{
                username = it.email?.split("@")?.get(0) ?: "unknown_username"
                Log.i(auth, "sign in method, user is $username")
                localStorage.saveUser(User(username, 0.0))
            } ?: run {
                Toast.makeText(applicationContext, "Please choose an account.", LENGTH_SHORT).show()
                signIn()
            }

        }
    }

    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap.apply {
            uiSettings.isZoomControlsEnabled = true
            setOnMarkerClickListener(this@MapsActivity)
        }
        setUpMap()
    }

    override fun onMarkerClick(p0: Marker?) = false

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        map.isMyLocationEnabled = true
        map.mapType = GoogleMap.MAP_TYPE_NORMAL

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                placeMarkerOnMap(currentLatLng)
                launch {
                    routeService.getQueriesInArea(currentLatLng) { route ->
                        launch {
                            route.toWayPoints().getRouteOnMap(map, apiKey)
                        }
                    }
                }
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
                routeService
            }
        }
    }

    private fun placeMarkerOnMap(location: LatLng) {
        routeCreator.addPoint(location)
    }

    private fun getAddress(latLng: LatLng): String {
        val geocoder = Geocoder(this)
        val addresses: List<Address>?
        val address: Address?
        var addressText = ""

        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (null != addresses && !addresses.isEmpty()) {
                address = addresses[0]
                for (i in 0 until address.maxAddressLineIndex) {
                    addressText += if (i == 0) address.getAddressLine(i) else "\n" + address.getAddressLine(i)
                }
            }
        } catch (e: IOException) {
            Log.e("MapsActivity", e.localizedMessage)
        }

        return addressText
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(this@MapsActivity,
                            REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    private fun loadPlacePicker() {
        val builder = PlacePicker.IntentBuilder()

        try {
            startActivityForResult(builder.build(this@MapsActivity), PLACE_PICKER_REQUEST)
        } catch (e: GooglePlayServicesRepairableException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }
    }

    private fun loadActiveRoutes(){
        
    }
}
