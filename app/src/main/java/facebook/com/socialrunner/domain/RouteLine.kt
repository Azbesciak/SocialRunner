package facebook.com.socialrunner.domain

import com.google.android.gms.maps.model.PolylineOptions
import facebook.com.socialrunner.domain.data.entity.Route

data class RouteLine(
        val polynomial: PolylineOptions,
        val color: Int,
        val route: Route
)