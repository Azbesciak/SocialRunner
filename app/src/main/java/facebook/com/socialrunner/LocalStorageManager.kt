package facebook.com.socialrunner

import android.content.Context
import android.util.Log
import facebook.com.socialrunner.domain.data.localdata.User
import java.io.*

class LocalStorageManager(val filename : String, val context: Context)
{
    fun saveUserdata(user : User)
    {
        try{
            val file = OutputStreamWriter(context.openFileOutput(filename, Context.MODE_PRIVATE))
            file.write("${user.name};${user.pace}")
            file.close()
        }catch(e:IOException)
        {
            Log.i("storage", "${e.message}")
        }
    }

    fun loadUserdata() : User? {
        return try {
            val data = InputStreamReader(context.openFileInput(filename)).readText().split(';')
            if(data.size >= 2)
                User(data[0], data[1].toDouble())
            else
                null
        }catch (e : IOException)
        {
            null
        }
    }

}