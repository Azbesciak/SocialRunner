package facebook.com.socialrunner.domain.data.entity


class Runner(id: String? = null,
             var name: String,
             var location: Location? = null,
             var routeIds: MutableList<String>
) : Entity(id)
