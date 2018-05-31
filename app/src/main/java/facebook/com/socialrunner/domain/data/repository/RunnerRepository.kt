package facebook.com.socialrunner.domain.data.repository


import facebook.com.socialrunner.domain.data.entity.Entity
import facebook.com.socialrunner.domain.data.entity.Position
import facebook.com.socialrunner.domain.data.entity.Runner

class RunnerRepository : FirebaseRepository<Entity>(RUNNER_ENTITY_PATH) {

    fun fetchByName(name: String, handler: ResultHandler<Runner>) {
        fetchByField("name", name, handler)
    }

    fun fetchLocation(username: String, handler: ResultHandler<String>) {
        fetchByName(username, ResultHandler { runner ->
            runner?.let {
                fetchByPath(getPathWithId(runner.id!!) + "/position", handler)
            }
        })
    }

    fun updateLocation(runnerId: String, pos: Position) {
        db.getReference(getPathWithId(runnerId) + "/position").setValue(pos)
    }

    companion object {
        private const val RUNNER_ENTITY_PATH = "runners"
    }
}
