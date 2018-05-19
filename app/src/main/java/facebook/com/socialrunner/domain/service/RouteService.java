package facebook.com.socialrunner.domain.service;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Iterator;
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

            Iterator<String> routeIdsIterator = runner.getRouteIds().iterator();
            List<Route> routes = new ArrayList<>();

            ResultHandler<Route> routeHandler = new ResultHandler<>();
            routeHandler.setOnDataChange(route -> {

                routes.add(route);

                if (routeIdsIterator.hasNext())
                    routeRepository.fetchById(routeIdsIterator.next(), routeHandler);
            });

            if (routeIdsIterator.hasNext())
                routeRepository.fetchById(routeIdsIterator.next(), routeHandler);

            onFetch.accept(routes);
        });

        runnerRepository.fetchByName(username, runnerHandler);
    }

    public void uploadNewRoute(String username, List<LatLng> waypoints) {

        ResultHandler<Runner> handler = new ResultHandler<>(runner -> {
            Route route = new Route();
            List<RoutePoint> routePoints =
                    waypoints.stream()
                            .map(loc -> new RoutePoint(loc.latitude, loc.longitude))
                            .collect(Collectors.toList());

            route.setRoutePoints(routePoints);

            routeRepository.create(route);

            runner.getRouteIds().add(route.getId());
            runnerRepository.update(runner);
        });

        runnerRepository.fetchByName(username, handler);
    }
}
