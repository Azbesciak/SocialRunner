package facebook.com.socialrunner

import android.content.Context
import android.widget.Toast

fun showToast(message: String, applicationContext: Context) {
    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
}