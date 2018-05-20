package facebook.com.socialrunner.domain.service

import com.google.android.gms.maps.model.LatLng
import facebook.com.socialrunner.domain.data.entity.Position
import facebook.com.socialrunner.domain.data.entity.Route
import facebook.com.socialrunner.domain.data.entity.RoutePoint
import facebook.com.socialrunner.domain.data.entity.Runner
import facebook.com.socialrunner.domain.data.repository.ResultHandler
import facebook.com.socialrunner.domain.data.repository.RouteRepository
import facebook.com.socialrunner.domain.data.repository.RunnerRepository
import java.util.*


class RouteService {

    private val routeRepository: RouteRepository = RouteRepository()
    private val runnerRepository: RunnerRepository = RunnerRepository()

    fun getRoutesForUser(username: String, onFetch: (List<Route>) -> Unit) {

        val runnerHandler = ResultHandler<Runner> { runner ->

            val routes = mutableMapOf<String, Route>()
            runner?.let {
                it.routeIds.forEach { routeId ->
                    routeRepository.fetchById(routeId, ResultHandler { route ->
                        routes[routeId] = route!!
                        onFetch(ArrayList(routes.values))
                    })
                }
            }
        }

        runnerRepository.fetchByName(username, runnerHandler)
    }

    fun uploadNewRoute(username: String, route: Route, waypoints: MutableList<LatLng>) {

        val handler = ResultHandler<Runner> { runner ->
            route.apply {
                route.routePoints.addAll(parseCoordinates(waypoints))
            }
            routeRepository.create(route)

            if (runner != null) {
                runner.routeIds.add(route.id!!)
                runnerRepository.update(runner)
            }
        }

        runnerRepository.fetchByName(username, handler)
    }

    private fun parseCoordinates(coordinates: MutableList<LatLng>) =
            coordinates.map { RoutePoint(loc = Position(it.latitude, it.longitude)) }
                    .toMutableList()
}
