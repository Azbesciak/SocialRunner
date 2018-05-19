package facebook.com.socialrunner.domain.service.route;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import facebook.com.socialrunner.domain.data.entity.Route;
import facebook.com.socialrunner.domain.data.entity.RoutePoint;
import facebook.com.socialrunner.domain.data.entity.Runner;
import facebook.com.socialrunner.domain.data.repository.ResultHandler;
import facebook.com.socialrunner.domain.data.repository.RouteRepository;
import facebook.com.socialrunner.domain.data.repository.RunnerRepository;

public class RouteService {

    private RouteRepository routeRepository;
    private RunnerRepository runnerRepository;

    // Dependency inversion compromised
    public RouteService() {
        this.routeRepository = new RouteRepository();
        this.runnerRepository = new RunnerRepository();
    }

    public void getRoutesForUser(String username, Consumer<List<Route>> onFetch) {

        ResultHandler<List<Route>> handler = new ResultHandler<>(onFetch);
        ResultHandler<Runner> runnerHandler = new ResultHandler<>(runner -> {
        });

        runnerRepository.fetchByName(username, runnerHandler);
    }

    public Route getRoute(String id) {
        return null;
    }

    public void uploadNewRoute(String username, List<LatLng> waypoints) {

        ResultHandler<Runner> handler = new ResultHandler<>(runner -> {
            Route route = new Route();
            List<RoutePoint> routePoints =
                    waypoints.stream().map(x -> new RoutePoint(x.latitude, x.longitude)).collect(Collectors.toList());

            routeRepository.create(route);

            runner.getRouteIds().add(route.getId());
            runnerRepository.update(runner);
        });

        runnerRepository.fetchByName(username, handler);
    }
}
