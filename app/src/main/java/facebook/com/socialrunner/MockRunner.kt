package facebook.com.socialrunner

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import facebook.com.socialrunner.domain.data.entity.Position
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

class MockRunner(var mapActivity : MapsActivity){
    public var waypoint : MutableList<Position> = arrayListOf()
    public lateinit var onPositionChgange : (pos : Position) -> Unit
    public lateinit var name : String
    private var pos = 0

    public fun setWaypoints(pos: List<Position>) : MockRunner
    {
        waypoint.addAll(pos)
        return this
    }
    public fun setUsername(name : String) : MockRunner
    {
        this.name = name
        return this
    }
    var lastMarker : LatLng = LatLng(0.0,0.0)
    public fun run(){
        launch(UI){
            while(pos < waypoint.size - 1)
            {
                var coef = 0.0f
                while(coef < 1.0001f)
                {
                    var tempPos = Position(waypoint[pos].latitude + coef*(waypoint[pos+1].latitude - waypoint[pos].latitude), waypoint[pos].longitude+coef*(waypoint[pos+1].longitude- waypoint[pos].longitude))
                    Log.i("pos2", "New position is ${tempPos.longitude} ${tempPos.latitude}")
                    mapActivity.removeMarker(lastMarker)
                    lastMarker = LatLng(tempPos.latitude, tempPos.longitude)
                    mapActivity.placeMarkerOnMap(lastMarker)
                    coef+=0.05f
                    Thread.sleep(2000)
                }
                mapActivity.removeMarker(lastMarker)
                pos += 1
            }
        }

    }

}