package facebook.com.socialrunner.domain.data.repository


import facebook.com.socialrunner.domain.data.entity.Runner

class RunnerRepository : FirebaseRepository<Runner>(RUNNER_ENTITY_PATH) {

    fun fetchByName(name: String, handler: ResultHandler<Runner>) {
        fetchByPath("$RUNNER_ENTITY_PATH/$name", handler)
    }

    fun fetchLocation(username: String, handler: ResultHandler<String>) {

        fetchByName(username, ResultHandler { runner ->

            runner?.let {
                fetchByPath(getPathWithId(runner.id!!) + "username", handler)
            }
        })
    }

    companion object {
        private const val RUNNER_ENTITY_PATH = "runners"
    }
}
