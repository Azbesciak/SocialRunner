package facebook.com.socialrunner.domain.data.repository

import android.util.Log
import com.google.firebase.database.*
import facebook.com.socialrunner.domain.data.entity.Position
import facebook.com.socialrunner.domain.data.entity.Route
import facebook.com.socialrunner.domain.data.entity.Runner

class FirebaseManager(val database: FirebaseDatabase,
                      val onRouteAdded : (Route) -> Unit,
                      val onRunnerAdded : (Runner) -> Unit)
{
    val runnersReference = "runners"
    val routesReference = "routes"
    private val runnersRef by lazy {
        database.getReference(runnersReference)
    }
    private val routesRef by lazy {
        database.getReference(routesReference)
    }

    lateinit var onRunnerRemoved : (Runner) -> Unit
    lateinit var onRunnerChanged : (Runner) -> Unit
    lateinit var onRouteRemoved : (Route) -> Unit
    lateinit var onRouteChanged : (Route) -> Unit

    init{
        runnersRef.addChildEventListener(object : ChildEventListener{
            override fun onCancelled(runner: DatabaseError) {
                Log.i("firebase runner's child", "cancelled $runner")
            }

            override fun onChildMoved(runner: DataSnapshot, p1: String?) {
                Log.i("firebase runner's child", "moved $runner")
            }

            override fun onChildChanged(runner: DataSnapshot, p1: String?) {
                runner.getValue(Runner::class.java)?.let {
                    onRunnerChanged?.invoke(it.setID(runner.key!!, it))
                }
            }

            override fun onChildAdded(runner: DataSnapshot, p1: String?) {
                runner.getValue(Runner::class.java)?.let {
                    onRunnerAdded?.invoke(it.setID(runner.key!!, it))
                }
            }

            override fun onChildRemoved(runner: DataSnapshot) {
                runner.getValue(Runner::class.java)?.let {
                    onRunnerRemoved?.invoke(it.setID(runner.key!!, it))
                }
            }

        })
        routesRef.addChildEventListener(object : ChildEventListener{
            override fun onCancelled(route: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildMoved(route: DataSnapshot, p1: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildChanged(route: DataSnapshot, p1: String?) {
                route.getValue(Route::class.java)?.let{
                    onRouteChanged?.invoke(it.setID(route.key!!, it))
                }
            }

            override fun onChildAdded(route: DataSnapshot, p1: String?) {
                route.getValue(Route::class.java)?.let{
                    onRouteAdded?.invoke(it.setID(route.key!!, it))
                }
            }

            override fun onChildRemoved(route: DataSnapshot) {
                route.getValue(Route::class.java)?.let{
                    onRouteRemoved?.invoke(it.setID(route.key!!, it))
                }
            }

        })
    }

    fun addRunner(runner : Runner) = database.getReference(runnersReference).push().setValue(runner)
    fun updateRunner(runner : Runner) = database.getReference("$runnersReference/${runner.id}").setValue(runner)
    fun updateRunnerPosition(runner : Runner, position: Position)
    {
        //may be one line but could not read to Position object instead of reading to latitude and longitude
        database.getReference("$runnersReference/${runner.id}/latitude").setValue(position.latitude)
        database.getReference("$runnersReference/${runner.id}/longitude").setValue(position.longitude)
    }
    fun removeRunner(runner : Runner) = database.getReference("$runnersReference/${runner.id}").removeValue()


    fun addRoute(route: Route) = database.getReference(routesReference).push().setValue(route)
    fun updateRoute(route: Route) = database.getReference("$routesReference/${route.id}").setValue(route)
    fun removeRoute(route: Route) = database.getReference("$routesReference/${route.id}").removeValue()

}