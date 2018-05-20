package facebook.com.socialrunner

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
     1   val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("facebook.com.socialrunner", appContext.packageName)
    }
}
