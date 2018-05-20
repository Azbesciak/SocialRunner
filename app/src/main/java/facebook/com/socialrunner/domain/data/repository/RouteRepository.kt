package facebook.com.socialrunner.domain.data.repository

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Query
import facebook.com.socialrunner.domain.data.entity.Route

class RouteRepository : FirebaseRepository<Route>(ROUTE_ENTITY_PATH) {
    companion object {
        private const val ROUTE_ENTITY_PATH = "routes"
    }

    fun fetchFromRoutes(handler: ResultHandler<Map<String, Route>>, mapper: (DatabaseReference) -> Query) =
            fetchByPath(ROUTE_ENTITY_PATH, handler, mapper)
}
