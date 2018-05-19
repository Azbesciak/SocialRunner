package facebook.com.socialrunner.domain.data.entity;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Route extends Entity {

    private List<RoutePoint> routePoints;
}
