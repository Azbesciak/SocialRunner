package facebook.com.socialrunner.domain.service;

import java.util.ArrayList;

import facebook.com.socialrunner.domain.data.entity.Runner;
import facebook.com.socialrunner.domain.data.repository.RunnerRepository;

public class RunnerService {

    private RunnerRepository runnerRepository;

    public RunnerService() {
        runnerRepository = new RunnerRepository();
    }

    public void registerRunner(String username) {

        Runner runner = new Runner(username, new ArrayList<>());
        runnerRepository.create(runner);
    }
}
