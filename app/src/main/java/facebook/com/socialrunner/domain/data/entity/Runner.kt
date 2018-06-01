package facebook.com.socialrunner.domain.data.entity


data class Runner(
        var name: String = "",
        var routeIds: MutableList<String> = arrayListOf(),
        var pace: Double = 0.0,
        var position: Position? = null,
        var isRunning: Boolean = false
)