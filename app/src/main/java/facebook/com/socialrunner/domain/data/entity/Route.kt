package facebook.com.socialrunner.domain.data.entity

import com.google.firebase.database.GenericTypeIndicator


class Route(id: String? = null,
            var pace: Double? = null,
            var startHour: Int? = null,
            var startMinute: Int? = null,
            var routePoints: MutableList<RoutePoint> = mutableListOf()) : Entity(id) {
    companion object {
        val listType = GenericTypeIndicator<List<Route>>()
        val objType = GenericTypeIndicator<Route>()
    }
}