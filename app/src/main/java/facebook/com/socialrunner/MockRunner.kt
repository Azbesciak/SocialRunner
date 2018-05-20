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
    public var waypoint : MutableList<RoutePoint> = arrayListOf()
    public lateinit var onPositionChgange : (pos : Position) -> Unit
    public lateinit var name : String
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
    public fun run(){
        launch(UI){
            while(pos < waypoint.size - 1)
            {
                var coef = 0.0f
                while(coef < 1.0001f)
                {
                    var tempPos = Position(waypoint[pos].loc.latitude + coef*(waypoint[pos+1].loc.latitude - waypoint[pos].loc.latitude),
                            waypoint[pos].loc.longitude+coef*(waypoint[pos+1].loc.longitude- waypoint[pos].loc.longitude))
                    Log.i("pos2", "New position is ${tempPos.longitude} ${tempPos.latitude}")
                    lastMarker.visible(false)
                   //  = LatLng(tempPos.latitude, tempPos.longitude)
                   // lastMarker = mapActivity.placeMarkerOnMap(lastMarker)
                    coef+=0.05f
                    Thread.sleep(2000)
                }
                //mapActivity.removeMarker(lastMarker)
                pos += 1
            }
        }

    }

}