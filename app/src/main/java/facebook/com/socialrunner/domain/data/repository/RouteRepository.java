package facebook.com.socialrunner.domain.data.repository;

import facebook.com.socialrunner.domain.data.entity.Route;

public class RouteRepository extends FirebaseRepository<Route> {

    private static final String ROUTE_ENTITY_PATH = "routes";

    public RouteRepository() {
        super(ROUTE_ENTITY_PATH);
    }
}
