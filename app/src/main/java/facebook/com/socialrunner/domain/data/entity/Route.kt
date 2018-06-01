package facebook.com.socialrunner.domain.data.entity

import org.joda.time.DateTime

data class Route(
        var id: String? = null,
        var pace: Double? = null,
        var creationTimestamp: Long? = DateTime.now().millis,
        var startHour: Int? = null,
        var startMinute: Int? = null,
        var leader: User? = null,
        var routePoints: MutableList<Position> = mutableListOf()) {
    fun currentTime() {
        DateTime.now().run {
            startMinute = minuteOfHour
            startHour = hourOfDay
        }
    }
}