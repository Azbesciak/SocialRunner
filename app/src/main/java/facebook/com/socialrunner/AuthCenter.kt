package facebook.com.socialrunner

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import facebook.com.socialrunner.domain.data.localdata.User
import java.util.*
import kotlin.math.abs

class AuthCenter(private val applicationContext: Context,
                 activity: Activity,
                 private val signInAction: (Intent) -> Unit) {
    companion object {
        private const val AUTH_TOKEN = "auth"
    }
    private var localStorage: LocalStorageManager
    lateinit var username: String
    private var mGoogleSignInClient: GoogleSignInClient

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
        mGoogleSignInClient = GoogleSignIn.getClient(activity, gso)
        localStorage = LocalStorageManager("user_data", applicationContext)
    }

    fun onResult( data: Intent) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        val account: GoogleSignInAccount?
        try {
            account = task.getResult(ApiException::class.java)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(AUTH_TOKEN, "signInResult:failed code=" + e.statusCode)
            showToast("Something went wrong, choosing random username.", applicationContext)
            username = "user_${abs(Random().nextInt() % 1000000)}"
            localStorage.saveUser(User(username, 0.0))
            return
        }
        account?.let {
            username = it.email?.split("@")?.get(0) ?: "unknown_username"
            Log.i(AUTH_TOKEN, "sign in method, user is $username")
            localStorage.saveUser(User(username, 0.0))
        } ?: run {
            showToast("Please choose an account.", applicationContext)
            signInAction(mGoogleSignInClient.signInIntent)
        }
    }

    fun saveUser(pace: Double) {
        localStorage.saveUser(User(username, pace))
    }

    fun loadUser(): User? = localStorage.loadUser()?.also { username = it.name }

    fun signIn() {
        val account = GoogleSignIn.getLastSignedInAccount(applicationContext)
        Log.i(AUTH_TOKEN, "$account")
        if (account == null) {
            signInAction(mGoogleSignInClient.signInIntent)
        } else {
            username = account.email?.split("@")?.get(0) ?: "unknown_username"
            Log.i(AUTH_TOKEN, "saved authenticated user is $username")
            saveUser(0.0)
        }
    }
}