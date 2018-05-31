package facebook.com.socialrunner.domain.data.repository

import facebook.com.socialrunner.domain.data.entity.Entity
import facebook.com.socialrunner.domain.data.entity.Route

class RouteRepository : FirebaseRepository<Entity>(ROUTE_ENTITY_PATH) {
    companion object {
        private const val ROUTE_ENTITY_PATH = "routes"
    }

    fun fetchFromRoutes(handler: ResultHandler<Map<String, Map<*, *>>>) =
            fetchByPath(ROUTE_ENTITY_PATH, handler)
}
