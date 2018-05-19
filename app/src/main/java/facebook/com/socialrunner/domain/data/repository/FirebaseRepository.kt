package facebook.com.socialrunner.domain.data.repository

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

import facebook.com.socialrunner.domain.data.entity.Entity

abstract class FirebaseRepository<T : Entity>(private val entityPath: String) {

    private var db: FirebaseDatabase = FirebaseDatabase.getInstance()

    protected fun <R> fetchByPath(path: String, handler: ResultHandler<R>) {
        val query = db.getReference(path)
        query.addValueEventListener(handler.asListener())
    }

    protected fun <R> fetchByPath(path: String, handler: ResultHandler<R>, mapper: (DatabaseReference) -> DatabaseReference) {
        var query = db.getReference(path)
        query = mapper(query)
        query.addValueEventListener(handler.asListener())
    }

    fun fetch(path: String, handler: ResultHandler<List<T>>) {
        fetchByPath(entityPath, handler)
    }

    fun fetchById(id: String, handler: ResultHandler<T>) {
        val path = getPathWithId(id)
        fetchByPath(path, handler)
    }

    fun create(entity: T) {
        if (entity.id != null)
            throw IllegalStateException("Entity already saved")

        val newEntity = db.getReference(entityPath).push()
        entity.id = newEntity.key
        newEntity.setValue(entity)
    }

    fun update(entity: T) {
        if (entity.id == null)
            throw IllegalStateException("Entity not saved")

        val reference = db.getReference(getPathWithId(entity.id!!))
        reference.setValue(entity)
    }

    fun delete(id: String?) {
        if (id != null)
            db.getReference(getPathWithId(id)).removeValue()
    }

    private fun getPathWithId(id: String): String {
        return String.format("%s/%s", entityPath, id)
    }
}
