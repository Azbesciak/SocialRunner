package facebook.com.socialrunner.domain.data.repository

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import facebook.com.socialrunner.Sup
import facebook.com.socialrunner.domain.data.entity.Position
import facebook.com.socialrunner.domain.data.entity.Route
import facebook.com.socialrunner.domain.data.entity.Runner

abstract class FirebaseStorage<T>(protected val database: FirebaseDatabase,
                                  private val clas: Class<T>,
                                  protected val refRoot: String,
                                  private val onAdd: Sup<T>,
                                  private val onRemove: Sup<T>,
                                  private val onChange: Sup<T>) {
    val dbRef by lazy {
        database.getReference(refRoot)
    }
    private val listener: ChildEventListener = object : ChildEventListener {
        override fun onCancelled(route: DatabaseError) {}

        override fun onChildMoved(route: DataSnapshot, p1: String?) {}

        override fun onChildChanged(snap: DataSnapshot, p1: String?) {
            snap.getValue(clas)?.let {
                onChange(modify(it, snap))
            }
        }

        override fun onChildAdded(snap: DataSnapshot, p1: String?) {
            snap.getValue(clas)?.let {
                onAdd(modify(it, snap))
            }
        }

        override fun onChildRemoved(snap: DataSnapshot) {
            snap.getValue(clas)?.let {
                onRemove(modify(it, snap))
            }
        }
    }

    protected abstract fun modify(t: T, snap: DataSnapshot): T

    fun init() {
        dbRef.addChildEventListener(listener)
    }

    fun cleanUp() {
        dbRef.removeEventListener(listener)
    }

    fun addRoute(route: Route) = database.getReference(refRoot).push().setValue(route)
}

class RunnersManager(database: FirebaseDatabase,
                     onRunnerAdded: Sup<Runner>,
                     onRunnerRemoved: Sup<Runner>,
                     onRunnerChanged: Sup<Runner>)
    : FirebaseStorage<Runner>(database, Runner::class.java, "runners",
        onRunnerAdded, onRunnerRemoved, onRunnerChanged) {
    override fun modify(t: Runner, snap: DataSnapshot): Runner {
        return t
    }

    fun updateRunner(runner: Runner) = database.getReference("$refRoot/${runner.name}").setValue(runner)
    fun updateRunnerPosition(runner: Runner, position: Position) {
        //may be one line but could not read to Position object instead of reading to latitude and longitude
        database.getReference("$refRoot/${runner.name}/latitude").setValue(position.latitude)
        database.getReference("$refRoot/${runner.name}/longitude").setValue(position.longitude)
    }

    fun removeRunner(runner: Runner) = database.getReference("$refRoot/${runner.name}").removeValue()
}

class RoutesStorage(database: FirebaseDatabase,
                    onRouteAdded: Sup<Route>,
                    onRouteRemoved: Sup<Route> = {},
                    onRouteChanged: Sup<Route> = {},
                    region: String) : FirebaseStorage<Route>(database, Route::class.java,
        "routes/$region", onRouteAdded, onRouteRemoved, onRouteChanged) {
    override fun modify(t: Route, snap: DataSnapshot): Route {
        t.id = snap.key!!
        return t
    }


    fun updateRoute(route: Route) = database.getReference("$refRoot/${route.id}").setValue(route)
    fun removeRoute(route: Route) = database.getReference("$refRoot/${route.id}").removeValue()
}