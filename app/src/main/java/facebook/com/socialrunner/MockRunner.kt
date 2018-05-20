package facebook.com.socialrunner

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import facebook.com.socialrunner.domain.data.entity.Position
import facebook.com.socialrunner.domain.data.entity.Route
import facebook.com.socialrunner.domain.data.entity.RoutePoint
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

class MockRunner(var mapActivity : MapsActivity){
    private var waypoint : MutableList<RoutePoint> = arrayListOf()
    lateinit var onPositionChgange : (pos : Position) -> Unit
    lateinit var name : String
    private var pos = 0

    public fun setRoute(route : Route) : MockRunner
    {
        waypoint.addAll(route.routePoints)
        return this
    }
    public fun setUsername(name : String) : MockRunner
    {
        this.name = name
        return this
    }
    var lastMarker : MarkerOptions = MarkerOptions()
    fun run(){
        var positionSet = false
        launch{
            while(pos < waypoint.size - 1)
            {
                var coef = 0.0f
                while(coef < 1.0001f)
                {
                    val tempPos = Position(waypoint[pos].loc.latitude + coef*(waypoint[pos+1].loc.latitude - waypoint[pos].loc.latitude),
                            waypoint[pos].loc.longitude+coef*(waypoint[pos+1].loc.longitude- waypoint[pos].loc.longitude))
                    Log.i("pos2", "New position is ${tempPos.longitude} ${tempPos.latitude}")

                    val pos = LatLng(tempPos.latitude, tempPos.longitude)
                    launch(UI) {
                        if (!positionSet) {
                            positionSet = true
                            lastMarker = mapActivity.placeMarkerOnMap(pos)
                        } else {
                            lastMarker.position(pos)
                        }
                    }
                    coef+=0.05f
                    delay(2000)
                }
                pos += 1
            }
        }

    }

}