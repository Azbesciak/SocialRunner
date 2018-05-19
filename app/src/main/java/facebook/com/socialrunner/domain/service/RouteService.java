package facebook.com.socialrunner.domain.service;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import facebook.com.socialrunner.domain.data.entity.Route;
import facebook.com.socialrunner.domain.data.entity.RoutePoint;
import facebook.com.socialrunner.domain.data.entity.Runner;
import facebook.com.socialrunner.domain.data.repository.ResultHandler;
import facebook.com.socialrunner.domain.data.repository.RouteRepository;
import facebook.com.socialrunner.domain.data.repository.RunnerRepository;
import facebook.com.socialrunner.util.backward.Consumer;

public class RouteService {

    private RouteRepository routeRepository;
    private RunnerRepository runnerRepository;

    // Dependency inversion compromised
    public RouteService() {
        this.routeRepository = new RouteRepository();
        this.runnerRepository = new RunnerRepository();
    }

    public void getRoutesForUser(String username, Consumer<List<Route>> onFetch) {

        ResultHandler<Runner> runnerHandler = new ResultHandler<>(runner -> {

            Map<String, Route> routes = new HashMap<>();

            for (String routeId : runner.getRouteIds()) {

                routeRepository.fetchById(routeId, new ResultHandler<Route>(route -> {
                    routes.put(routeId, route);
                    onFetch.accept(new ArrayList<>(routes.values()));
                }));
            }
        });

        runnerRepository.fetchByName(username, runnerHandler);
    }

    public void uploadNewRoute(String username, Route routeDetails, List<LatLng> waypoints) {

        ResultHandler<Runner> handler = new ResultHandler<>(runner -> {
            Route route = new Route();
            List<RoutePoint> routePoints =
                    waypoints.stream()
                            .map(loc -> new RoutePoint(loc.latitude, loc.longitude))
                            .collect(Collectors.toList());

            route.setRoutePoints(routePoints);
            route.setPace(routeDetails.getPace());
            route.setStartHour(routeDetails.getStartHour());
            route.setStartMinute(routeDetails.getStartMinute());
            routeRepository.create(route);
            if (runner != null) {
                runner.getRouteIds().add(route.getId());
                runnerRepository.update(runner);
            }
        });

        runnerRepository.fetchByName(username, handler);
    }

    private List<RoutePoint> parseCoordinates(List<LatLng> coordinates) {

        List<RoutePoint> routePoints = new ArrayList<>();

        for (LatLng coordinate : coordinates)
            routePoints.add(new RoutePoint(coordinate.latitude, coordinate.longitude));

        return routePoints;
    }
}
