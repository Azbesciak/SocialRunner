package facebook.com.socialrunner.domain.data.entity;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Runner extends Entity {

    private String name;
    private List<String> routeIds;
}
