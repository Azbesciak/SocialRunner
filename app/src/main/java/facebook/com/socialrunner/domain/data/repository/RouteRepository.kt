package facebook.com.socialrunner.domain.data.repository

import facebook.com.socialrunner.domain.data.entity.Route

class RouteRepository : FirebaseRepository<Route>(ROUTE_ENTITY_PATH) {
    companion object {

        private const val ROUTE_ENTITY_PATH = "routes"
    }
}