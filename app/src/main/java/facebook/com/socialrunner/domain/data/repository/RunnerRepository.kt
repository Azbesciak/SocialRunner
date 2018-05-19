package facebook.com.socialrunner.domain.data.repository


import facebook.com.socialrunner.domain.data.entity.Runner

class RunnerRepository : FirebaseRepository<Runner>(RUNNER_ENTITY_PATH) {

    fun fetchByName(name: String, handler: ResultHandler<Runner>) {
        fetchByPath("$RUNNER_ENTITY_PATH/$name", handler)
    }

    companion object {

        private const val RUNNER_ENTITY_PATH = "runners"
    }
}
