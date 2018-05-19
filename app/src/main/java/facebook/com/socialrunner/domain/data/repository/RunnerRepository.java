package facebook.com.socialrunner.domain.data.repository;

import com.google.firebase.database.DatabaseReference;

import facebook.com.socialrunner.domain.data.entity.Runner;

public class RunnerRepository extends FirebaseRepository<Runner> {

    private static final String RUNNER_ENTITY_PATH = "runners";

    public RunnerRepository() {
        super(RUNNER_ENTITY_PATH);
    }

    public void fetchByName(String name, ResultHandler<Runner> handler) {
        fetchByPath(RUNNER_ENTITY_PATH + "/" + name, handler);
    }
}
