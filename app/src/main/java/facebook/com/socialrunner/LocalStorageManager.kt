package facebook.com.socialrunner

import android.content.Context
import android.util.Log
import facebook.com.socialrunner.domain.data.entity.User
import java.io.*

class LocalStorageManager(private val filename: String, private val context: Context) {
    private var user: User? = null
    fun saveUser(user: User) {
        try {
            val file = OutputStreamWriter(context.openFileOutput(filename, Context.MODE_PRIVATE))
            file.write("${user.name};${user.pace}")
            file.close()
            this.user = user
        } catch (e: IOException) {
            Log.i("storage", "${e.message}")
        }
    }

    fun loadUser(): User? {
        if (user == null) {
            try {
                val data = InputStreamReader(context.openFileInput(filename)).readText().split(';')
                if (data.size >= 2)
                    user = User(data[0], data[1].toDouble())
            } catch (e: IOException) {
            }
        }
        return user
    }
}