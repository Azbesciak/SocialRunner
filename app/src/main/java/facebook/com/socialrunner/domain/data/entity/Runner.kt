package facebook.com.socialrunner.domain.data.entity

import com.google.firebase.database.GenericTypeIndicator


class Runner(id: String? = null,
             var name: String,
             var position: Position? = null,
             var routeIds: MutableList<String>
) : Entity(id) {
    companion object {
        val objType = Runner("s","s",Position(1.0,1.0), routeIds = mutableListOf()).javaClass
    }
}
