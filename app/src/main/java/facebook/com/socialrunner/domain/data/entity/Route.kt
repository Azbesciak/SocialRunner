package facebook.com.socialrunner.domain.data.entity


class Route(id: String? = null,
            var pace: Double? = null,
            var startHour: Int? = null,
            var startMinute: Int? = null,
            var routePoints: MutableList<RoutePoint> = mutableListOf()) : Entity(id)