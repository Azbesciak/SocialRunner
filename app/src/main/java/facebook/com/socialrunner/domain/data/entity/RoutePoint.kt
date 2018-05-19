package facebook.com.socialrunner.domain.data.entity

import lombok.AllArgsConstructor
import lombok.Data
import lombok.NoArgsConstructor

class RoutePoint(id: String? = null, val latitude: Double = 0.toDouble(), val longitude: Double = 0.toDouble()) : Entity(id)
