package facebook.com.socialrunner.domain.data.repository;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import facebook.com.socialrunner.domain.data.entity.Entity;

public abstract class FirebaseRepository<T extends Entity> {

    protected FirebaseDatabase db;
    private String entityPath;

    public FirebaseRepository(String entityPath) {
        this.db = FirebaseDatabase.getInstance();
        this.entityPath = entityPath;
    }

    protected void watch(String path, ResultHandler<T> handler) {

        DatabaseReference query =  db.getReference(path);
        query.addValueEventListener(handler.asListener());
    }

    protected void watchById(String id, ResultHandler<T> handler) {

        String path = getPathWithId(id);
        watch(path, handler);
    }

    protected void create(T entity) {
        if (entity.getId() != null)
            throw new IllegalStateException("Entity already saved");

        DatabaseReference newEntity = db.getReference(entityPath).push();

        entity.setId(newEntity.getKey());
        newEntity.setValue(newEntity);
    }

    protected void update(T entity) {
        if (entity.getId() == null)
            throw new IllegalStateException("Entity not saved");

        DatabaseReference reference = db.getReference(getPathWithId(entity.getId()));
        reference.setValue(entity);
    }

    protected void delete(String id) {
        if (id != null)
            db.getReference(getPathWithId(id)).removeValue();
    }

    protected String getPathWithId(String id) {
        return String.format("%s/%s", entityPath, id);
    }
}
