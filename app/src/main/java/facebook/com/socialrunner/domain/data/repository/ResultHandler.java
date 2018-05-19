package facebook.com.socialrunner.domain.data.repository;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.function.Consumer;

import facebook.com.socialrunner.domain.data.entity.Entity;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class ResultHandler<T> {

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
