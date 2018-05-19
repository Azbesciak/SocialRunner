package facebook.com.socialrunner.domain.data.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoutePoint extends Entity {

    private double latitude;
    private double longitude;
}
