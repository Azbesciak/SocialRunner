package facebook.com.socialrunner.domain.service

import facebook.com.socialrunner.domain.data.entity.Runner
import facebook.com.socialrunner.domain.data.repository.RunnerRepository

class RunnerService {

    private val runnerRepository: RunnerRepository = RunnerRepository()

    fun registerRunner(username: String) {

        val runner = Runner(name = username, routeIds = mutableListOf())
        runnerRepository.create(runner)
    }
}
