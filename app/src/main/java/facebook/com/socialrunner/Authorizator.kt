package facebook.com.socialrunner

import android.accounts.AccountManager
import android.content.Context
import android.util.Log
import java.util.*
import java.util.Random

public lateinit var username : String

fun getUsername(context: Context) {
    val manager = AccountManager.get(context)
    val accounts = manager.getAccountsByType("com.google")
    val possibleEmails = LinkedList<String>()

    for (account in accounts) {
        // TODO: Check possibleEmail against an email regex or treat
        // account.name as an email address only for certain account.type
        // values.
        possibleEmails.add(account.name)
        Log.i("auth", account.name)
    }

    if (!possibleEmails.isEmpty()) {
        val email = possibleEmails.get(0)

        val parts = email.split("@".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        if (parts.size > 0) {
            username = parts[0]
        } else {
            username = "user_${Random().nextInt() % 1000000}"
        }
    }
}