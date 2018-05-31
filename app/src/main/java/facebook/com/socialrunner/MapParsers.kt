package facebook.com.socialrunner

import facebook.com.socialrunner.domain.data.entity.Position
import facebook.com.socialrunner.domain.data.entity.Route
import facebook.com.socialrunner.domain.data.entity.RoutePoint

fun Map<*, Map<*,*>>.toRoutes(): List<Route> {
    return this.values.map {
        val vals = (it["routePoints"]) as List<Map<String, Map<String, Double>>>
        val routePoints = vals.flatMap { it.values }
                .map { RoutePoint(loc = Position(it["latitude"]!!, it["longitude"]!!)) }
                .toMutableList()
        Route(
                pace = it["pace"].toString().toDouble(),
                startHour = it["startHour"].toString().toInt(),
                startMinute = it["startMinute"].toString().toInt(),
                routePoints = routePoints)
    }.toList()
}
