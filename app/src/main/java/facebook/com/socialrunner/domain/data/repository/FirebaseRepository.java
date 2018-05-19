package facebook.com.socialrunner.domain.data.repository;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import facebook.com.socialrunner.domain.data.entity.Entity;

public abstract class FirebaseRepository<T extends Entity> {

    protected FirebaseDatabase db;
    private String entityPath;

    public FirebaseRepository(String entityPath) {
        this.db = FirebaseDatabase.getInstance();
        this.entityPath = entityPath;
    }

    protected <R> void fetchByPath(String path, ResultHandler<R> handler) {
        DatabaseReference query =  db.getReference(path);
        query.addValueEventListener(handler.asListener());
    }

    public void fetch(String path, ResultHandler<List<T>> handler) {
        fetchByPath(entityPath, handler);
    }

    public void fetchById(String id, ResultHandler<T> handler) {
        String path = getPathWithId(id);
        fetchByPath(path, handler);
    }

    public void create(T entity) {
        if (entity.getId() != null)
            throw new IllegalStateException("Entity already saved");

        DatabaseReference newEntity = db.getReference(entityPath).push();

        entity.setId(newEntity.getKey());
        newEntity.setValue(newEntity);
    }

    public void update(T entity) {
        if (entity.getId() == null)
            throw new IllegalStateException("Entity not saved");

        DatabaseReference reference = db.getReference(getPathWithId(entity.getId()));
        reference.setValue(entity);
    }

    public void delete(String id) {
        if (id != null)
            db.getReference(getPathWithId(id)).removeValue();
    }

    protected String getPathWithId(String id) {
        return String.format("%s/%s", entityPath, id);
    }
}
