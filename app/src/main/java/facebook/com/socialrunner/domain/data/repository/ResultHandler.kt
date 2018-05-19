package facebook.com.socialrunner.domain.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ResultHandler<T>(private val onDataChange: (T?) -> Unit) {

    private val onCancel: () -> Unit = {}

    fun asListener(): ValueEventListener {

        return object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                onDataChange(dataSnapshot.value as T?)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                onCancel()
            }
        }
    }
}
