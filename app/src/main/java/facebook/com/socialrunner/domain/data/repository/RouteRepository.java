package facebook.com.socialrunner.domain.data.repository;

import facebook.com.socialrunner.domain.data.entity.Route;

public class RouteRepository extends FirebaseRepository<Route> {

    public RouteRepository(String entityPath) {
        super(entityPath);
    }
}
