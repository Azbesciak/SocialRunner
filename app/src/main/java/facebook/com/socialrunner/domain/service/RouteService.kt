package facebook.com.socialrunner.domain.service

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import facebook.com.socialrunner.domain.data.entity.Position
import facebook.com.socialrunner.domain.data.entity.Route
import facebook.com.socialrunner.domain.data.entity.RoutePoint
import facebook.com.socialrunner.domain.data.entity.Runner
import facebook.com.socialrunner.domain.data.repository.ResultHandler
import facebook.com.socialrunner.domain.data.repository.RouteRepository
import facebook.com.socialrunner.domain.data.repository.RunnerRepository
import facebook.com.socialrunner.toRoutePoints
import facebook.com.socialrunner.toRoutes
import java.util.*


class RouteService {

    private val routeRepository: RouteRepository = RouteRepository()
    private val runnerRepository: RunnerRepository = RunnerRepository()

    fun getQueriesInArea(loc: LatLng, onFetch: (Route) -> Unit) {
        val loca = location(loc)
        val handler = ResultHandler<Map<String, Map<*, *>>> { routes ->
            routes?.run {
                toRoutes().filter { route -> route.routePoints.any { rp -> isInRange(rp, loca) } }
                        .forEach(onFetch)
            }
        }
        routeRepository.fetchFromRoutes(handler)
    }

    private fun isInRange(rp: RoutePoint, base: Location): Boolean {
        return location(rp).distanceTo(base) < 5000
    }

    private fun location(l: LatLng) = Location("").apply {
        latitude = l.latitude
        longitude = l.longitude
    }

    private fun location(rp: RoutePoint) = Location("").apply {
        latitude = rp.loc.latitude
        longitude = rp.loc.longitude
    }


    fun uploadNewRoute(username: String, route: Route, waypoints: MutableList<LatLng>) {
        route.routePoints.addAll(waypoints.toRoutePoints())

        val handler = ResultHandler<Runner> { runner ->
            //routeRepository.create(route)

//            if (runner != null) {
//                runner.routeIds.add(route.id!!)
//                runnerRepository.update(runner)
//            }
        }

        runnerRepository.fetchByName(username, handler)
    }

    private fun parseCoordinates(coordinates: MutableList<LatLng>) =
            coordinates.map { RoutePoint(loc = Position(it.latitude, it.longitude)) }
                    .toMutableList()
}
