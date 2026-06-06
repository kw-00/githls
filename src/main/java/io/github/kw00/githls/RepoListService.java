package io.github.kw00.githls;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RepoListService {
    private final GithubClient githubClient;

    @Autowired
    public RepoListService(GithubClient githubClient) {
        this.githubClient = githubClient;
    }

    public List<RepositoryDto> getRepositories(
        String userLogin
    ) throws UserNotFoundException, RateLimitExceededException {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var repositories = githubClient.getRepositories(userLogin);
            var tasks = new ArrayList<Future<RepositoryDto>>(repositories.size());
            for (var repo : repositories) {
                var task = executor.submit(() -> {
                    var branches = githubClient.getBranches(
                        repo.name(),
                        repo.owner().login()
                    );
                    return new RepositoryDto(repo, branches);
                });
                tasks.add(task);
            }
            var repoDtos = new ArrayList<RepositoryDto>(repositories.size());
            for (var task : tasks) {
                try {
                    repoDtos.add(task.get());
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof RepositoryNotFoundException)
                        continue;
                    throw new RuntimeException(e);
                }
            }
            return repoDtos;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } 
                
    }
}
