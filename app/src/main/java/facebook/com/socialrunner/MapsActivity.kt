package facebook.com.socialrunner

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityCompat.requestPermissions
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.location.places.ui.PlaceSelectionListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import facebook.com.socialrunner.MockRunnerService.running
import facebook.com.socialrunner.domain.RouteLine
import facebook.com.socialrunner.domain.data.entity.Position
import facebook.com.socialrunner.domain.data.entity.Route
import facebook.com.socialrunner.domain.data.entity.Runner
import facebook.com.socialrunner.domain.data.repository.RoutesStorage
import facebook.com.socialrunner.domain.data.repository.RunnersStorage
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private lateinit var map: GoogleMap
    private lateinit var geocoder: Geocoder
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var locationRequest: LocationRequest
    private lateinit var authCenter: AuthCenter
    private var locationUpdateState = false

    private val apiKey by lazy { getString(R.string.google_api_key) }
    private var searchRegion: String? = null
    private var runnerRegion: String? = null
    private var currentRunner: MapRunner? = null

    private val routeCreator by lazy {
        NewRouteCreator(map, apiKey, {
            sendRouteBtn.isEnabled = it.isNotEmpty()
        })
    }

    private val db by lazy {
        FirebaseApp.initializeApp(this)
        FirebaseDatabase.getInstance()
    }

    private var runnersStorage: RunnersStorage? = null
    private var routeStorage: RoutesStorage? = null
    private val gpsManager = GPSManager(::newPosition)

    private val otherRoutes = CopyOnWriteArrayList<RouteLine>()
    private val otherRunners = ConcurrentHashMap<String, MapRunner>()

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
        private const val PLACE_PICKER_REQUEST = 3
        private const val RC_SIGN_IN = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val permissions = PermissionsGuard(this)
        permissions.acquirePermissions()


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geocoder = Geocoder(applicationContext)
        initButtons()
        initAutocomplete()
        createLocationRequest()
        authCenter = AuthCenter(applicationContext, this, ::signIn)
    }

    private fun runnerChanged(runner: Runner) {
        ifNotCurrentRunner(runner) {
            otherRunners[runner.name]?.updatePosition(runner.position!!.toLatLng())
        }
        Log.i("datachagned", "runner ${runner.name} has been changed")
    }

    private fun runnerRemoved(runner: Runner) {
        otherRunners.remove(runner.name)
        Log.i("dataremoved", "runner ${runner.name} has been removed from database")
    }

    private fun newRunnerFetched(runner: Runner) {
        ifNotCurrentRunner(runner) {
            val mapRunner = MapRunner(runner, this)
            otherRunners[runner.name] = mapRunner
            mapRunner.updatePosition(runner.position!!.toLatLng())
        }
        Log.i("datafetched", "new runner fetched, runner's name ${runner.name}, position ${runner.position}, id ${runner.name}")

    }

    private fun ifNotCurrentRunner(runner: Runner, onDifferent: () -> Unit) {
        if (currentRunner?.runner?.name != runner.name) onDifferent()
    }

    private fun initAutocomplete() {
        val fragment = fragmentManager
                .findFragmentById(R.id.autocomplete_fragment) as PlaceAutocompleteFragment
        fragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(p0: Place?) {
                p0?.latLng?.run {
                    focusMapAt(this)
                }
            }

            override fun onError(p0: Status?) {}
        })
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
            disableSend()
            stopAdding()
        }
    }

    private fun disableSend() {
        if (::map.isInitialized) {
            routeCreator.disable()
        }
    }

    private fun startRun(pace: Double) {
        val route = Route().apply {
            currentTime()
            leader = authCenter.updatePace(pace)
            this.pace = pace
        }
        Log.i("run", "pace is $pace")
        routeCreator.send(routeStorage!!, route)
        disableSend()
    }

    private fun postponeRun(pace: Double) {
        Log.i("run", "pace is $pace")
        authCenter.updatePace(pace)
        TimePickerFragment()
                .setCallback(::runTimePicked)
                .show(fragmentManager, "this tag is awesome!")
    }

    private fun runTimePicked(hour: Int, minute: Int) {
        Log.i("run", "run time is set to $hour:$minute")
        val route = Route(startHour = hour, startMinute = minute, leader = authCenter.loadUser())
        routeCreator.send(routeStorage!!, route)
        disableSend()
    }

    private fun startAdding() {
        sendRouteBtn.isEnabled = false
        routeCreator.initialize()
        sendRouteBtn.show()
        cancelRouteBtn.show()
        createRouteBtn.hide()
    }

    private fun stopAdding() {
        sendRouteBtn.hide()
        cancelRouteBtn.hide()
        createRouteBtn.show()
    }

    private fun updateWorkerLocation() {
        launch(UI) {
            while (true) {
                delay(2500)
                searchNearby(map.cameraPosition.target)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        updateWorkerLocation()
        gpsManager.getPosition(this)
        authCenter.loadUser() ?: authCenter.signIn()
    }

    private fun signIn(signInIntent: Intent) {
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun newPosition(location: Location) {
        if (currentRunner == null) {
            val user = authCenter.loadUser() ?: return
            val runner = Runner(user.name, pace = user.pace, isRunning = true)
            currentRunner = MapRunner(runner, this)
        }
        val latLng = LatLng(location.latitude, location.longitude)
        onLocationChange(latLng, runnerRegion) { newRegion ->
            runnersStorage?.cleanUp(currentRunner!!.runner)
            runnerRegion = newRegion
            runnersStorage = RunnersStorage(db, ::newRunnerFetched, ::runnerRemoved, ::runnerChanged, newRegion)
            runnersStorage!!.updateRunner(currentRunner!!.runner)
        }
        currentRunner!!.run {
            runner.position = Position(latitude = location.latitude, longitude = location.longitude)
            runnersStorage?.updateRunnerPosition(runner)
            Log.i("gps", "New position in main lat:${location.latitude}, lon:${location.longitude}")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CHECK_SETTINGS -> {
                if (resultCode == Activity.RESULT_OK) {
                    locationUpdateState = true
                    startLocationUpdates()
                }
            }
            PLACE_PICKER_REQUEST -> {
                if (resultCode == RESULT_OK) {
                    val place = PlacePicker.getPlace(this, data)
                    var addressText = place.name.toString()
                    addressText += "\n" + place.address.toString()
                    addMarker(place.latLng)
                }
            }
            RC_SIGN_IN -> {
                authCenter.onResult(data)
            }
        }
    }

    private fun showToast(message: String) {
        showToast(message, this)
    }

    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        removeDbListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        removeDbListeners()
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
                        ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(this,
                    arrayOf(ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        map.isMyLocationEnabled = true
        map.mapType = GoogleMap.MAP_TYPE_NORMAL

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                searchForNearbyRoutes(currentLatLng)
                focusMapAt(currentLatLng)
                onPolylineClickListener()
            }
        }
    }

    fun focusMapAt(loc: LatLng) {
        searchNearby(loc)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 12f))
    }

    private fun onPolylineClickListener() {
        map.setOnPolylineClickListener { line ->
            val find = otherRoutes.find { it.route.id == line.tag }
            find?.run {
                with(find.route) {
                    showToast(
                            "Pace: $pace" +
                                    "\nStart: $startHour:$startMinute" +
                                    "\nRunner: ${leader!!.name}")
                }
            }
        }
    }

    private fun searchForNearbyRoutes(location: LatLng) {
        otherRoutes.clear()
        searchNearby(location)
    }

    fun addMarker(latLng: LatLng): Marker = map.addMarker(latLng.marker())

    private fun searchNearby(latLng: LatLng) {
        onLocationChange(latLng, searchRegion) { newRegion ->
            searchRegion = newRegion
            showToast("changed searchRegion to $searchRegion")
            routeStorage?.cleanUp()
            routeStorage = RoutesStorage(db, onRouteAdded = ::onNewRoute, region = newRegion)
        }
    }

    private fun onLocationChange(latLng: LatLng, curRegion: String?, onNewRegion: Sup<String>) {
        val location = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        if (location.isNotEmpty()) {
            val loc = location.first()
            val newRegion = loc.locality ?: loc.subAdminArea ?: loc.adminArea ?: loc.countryName
            ?: "unknown"
            if (curRegion != newRegion) {
                onNewRegion(newRegion)
            }
        }
    }

    private fun removeDbListeners() {
        routeStorage?.cleanUp()
        if (currentRunner != null && runnersStorage != null) {
            runnersStorage!!.cleanUp(currentRunner!!.runner)
        }
        routeStorage = null
        runnersStorage = null
    }

    private fun onNewRoute(route: Route) {
        val isAlready = otherRoutes.any { it.route.id == route.id }
        if (isAlready) return
        running(route)
        launch {
            route.toWayPoints().getRouteOnMap(map, apiKey) {
                first.color = colors[randGen.nextInt(colors.size)]
                first.isClickable = true
                first.tag = route.id
                val routeLine = RouteLine(second, first.color, route)
                otherRoutes.add(routeLine)
                routeLine.draw()
            }
        }
    }

    private fun RouteLine.draw() {
        map.addPolyline(polynomial).apply {
            color = this@draw.color
            isClickable = true
            tag = route.id
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                        ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(this,
                    arrayOf(ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE)
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
        LocationServices
                .getSettingsClient(this)
                .checkLocationSettings(builder.build())
                .apply {
                    addOnSuccessListener {
                        locationUpdateState = true
                        startLocationUpdates()
                    }
                    addOnFailureListener { e ->
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
    }
}
