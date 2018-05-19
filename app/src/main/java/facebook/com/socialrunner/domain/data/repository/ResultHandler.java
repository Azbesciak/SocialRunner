package facebook.com.socialrunner.domain.data.repository;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import facebook.com.socialrunner.domain.data.entity.Entity;
import facebook.com.socialrunner.util.backward.Consumer;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class ResultHandler<T extends Entity> {

    private Consumer<T> onDataChange;
    private Consumer<Void> onCancel;

    public ResultHandler(Consumer<T> onDataChange) {
        this.onDataChange = onDataChange;
    }

    public ValueEventListener asListener() {

        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (onDataChange != null)
                    onDataChange.accept((T) dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (onCancel != null)
                    onCancel.accept(null);
            }
        };
    }
}
