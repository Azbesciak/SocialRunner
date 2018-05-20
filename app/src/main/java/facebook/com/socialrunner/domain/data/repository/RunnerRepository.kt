package facebook.com.socialrunner.domain.data.repository


import facebook.com.socialrunner.domain.data.entity.Position
import facebook.com.socialrunner.domain.data.entity.Runner

class RunnerRepository : FirebaseRepository<Runner>(RUNNER_ENTITY_PATH) {

    fun fetchByName(name: String, handler: ResultHandler<Runner>) {
        fetchByField("name", name, handler)
    }

    fun fetchPosition(username: String, handler: ResultHandler<Position>) {

        fetchByName(username, ResultHandler { runner ->

            runner?.let {
                fetchByPath(getPathWithId(runner.id!!) + "/position", handler)
            }
        })
    }

    fun updateLocation(runnerId: String, loc: Position) {

        db.getReference(getPathWithId(runnerId) + "/username").setValue(loc)
    }

    companion object {
        private const val RUNNER_ENTITY_PATH = "runners"
    }
}
