package facebook.com.socialrunner.domain.service

import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.FirebaseDatabase
import facebook.com.socialrunner.domain.data.entity.Position
import facebook.com.socialrunner.domain.data.entity.Runner
import facebook.com.socialrunner.domain.data.repository.ResultHandler
import facebook.com.socialrunner.domain.data.repository.RunnerRepository

class RunnerService {

    private val runnerRepository: RunnerRepository = RunnerRepository()
    private val runnerNameIdCache: MutableMap<String, String> = mutableMapOf()

    fun registerRunner(username: String) {

        val runner = Runner(name = username, routeIds = mutableListOf())
        runnerRepository.create(runner)
    }

    fun getRunnerPosition(username : String, handler : ResultHandler<String>) {
        runnerRepository.fetchLocation(username, handler)
    }

    fun updateRunnerLocation(username: String, loc: Position) {
        if (!runnerNameIdCache.containsKey(username)) {
            runnerRepository.fetchByName(username, ResultHandler { runner ->
                runnerNameIdCache[username] = runner?.id!!
                updateLocationById(runner.id!!, loc)
            })
        } else {
            updateLocationById(runnerNameIdCache[username]!!, loc)
        }
    }

    private fun updateLocationById(runnerId: String, loc: Position) {
        runnerRepository.updateLocation(runnerId, loc)
    }
}
