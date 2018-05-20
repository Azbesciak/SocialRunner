package facebook.com.socialrunner

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import facebook.com.socialrunner.domain.data.entity.Position
import facebook.com.socialrunner.domain.data.entity.Runner
import facebook.com.socialrunner.domain.data.repository.ResultHandler
import facebook.com.socialrunner.domain.data.repository.RunnerRepository
import facebook.com.socialrunner.domain.service.RunnerService
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QueryPlaygroundTest {

    var db: FirebaseDatabase? = null

    @Before
    fun setUp() {
        val ctx = InstrumentationRegistry.getTargetContext()
        FirebaseApp.initializeApp(ctx)

        db = FirebaseDatabase.getInstance()
    }

    @Test
    fun testQuery() {
        val listener: V = V()
        /*RunnerService().getRunnerPosition("ktoś", ResultHandler<Position> { position ->
            Log.d("TEST", position.toString())
        })*/
        RunnerRepository().fetchByName("ktoś", ResultHandler { runner ->
            Log.d("TEST", runner.toString())
        })

        Thread.sleep(5000);
    }

    class V: ValueEventListener {
        override fun onCancelled(p0: DatabaseError?) {
            Log.e("TEST", "FETCH FAILED!!!");
        }

        override fun onDataChange(p0: DataSnapshot?) {
            Log.d("TEST", "FETCH SUCCEEDED!!!")
            Log.d("TEST", p0?.getValue().toString() ?: "null")
        }
    }

}