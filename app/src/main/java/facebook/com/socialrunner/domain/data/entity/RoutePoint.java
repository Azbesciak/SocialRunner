package facebook.com.socialrunner.domain.data.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RoutePoint extends Entity {

    private double latitude;
    private double longitude;
}
