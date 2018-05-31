package facebook.com.socialrunner.domain.data.entity

data class Route(
            var pace: Double? = null,
            var startHour: Int? = null,
            var startMinute: Int? = null,
            var routePoints: MutableList<RoutePoint> = mutableListOf()) : Entity()