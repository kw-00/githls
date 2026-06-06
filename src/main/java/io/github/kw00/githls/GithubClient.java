package io.github.kw00.githls;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

@Component
public class GithubClient {
    private static final Logger logger =
        LoggerFactory.getLogger(GithubClient.class);

    private static final Gson jsonParser = new Gson();
    private final HttpClient client = HttpClient.newBuilder()
        .followRedirects(Redirect.NORMAL)
        .connectTimeout(Duration.ofSeconds(20))
        .build();
    private final String githubApiBase;
    private final String githubApiToken;

    public GithubClient(
        @Value("${github.api-base}")
        String githubApiBase,
        @Value("${github.api-token}")
        String githubApiToken
    ) {
        Objects.requireNonNull(githubApiBase);
        this.githubApiBase = githubApiBase;
        this.githubApiToken = githubApiToken;
    }

    public List<Repository> getRepositories(String userLogin)
        throws UserNotFoundException, RateLimitExceededException {

        var uri = URI.create(
            githubApiBase
            + "/users/" + userLogin
            + "/repos"
            + "?type=all"
        );
        var request = getRequestBuilder()
            .uri(uri)
            .GET()
            .build();
        try {
            var response = this.client.send(request, BodyHandlers.ofString());
            logger.debug("Sent request to {}", request.uri());
            var status = response.statusCode();
            var body = response.body();
            logger.debug("Response status: {}", status);
            logger.debug("Response body: {}", body);
            if (status == 404)
                throw new UserNotFoundException(
                    "User with login of "
                    + userLogin
                    + " could not be found."
                );
            throwIfErrorStatus(status);
            var repositories = jsonParser.fromJson(body, Repository[].class);
            var result = new ArrayList<Repository>(repositories.length);
            Collections.addAll(result, repositories);
            logger.debug("Parsed {} repository objects", result.size());
            return result;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Branch> getBranches(
        String repositoryName,
        String repositoryOwnerLogin
    ) throws RepositoryNotFoundException, RateLimitExceededException {

        var uri = URI.create(
            githubApiBase
            + "/repos/"
            + repositoryOwnerLogin + "/" + repositoryName
            + "/branches"
        );
        var request = getRequestBuilder()
            .uri(uri)
            .GET()
            .build();
        try {
            var response = client.send(request, BodyHandlers.ofString());
            logger.debug("Sent request to {}", request.uri());
            var status = response.statusCode();
            var body = response.body();
            logger.debug("Response status: {}", status);
            logger.debug("Response body: {}", body);
            if (status == 404)
                throw new RepositoryNotFoundException(
                    "Repository under "
                    + repositoryOwnerLogin
                    + "/" + repositoryName
                    + " could not be found."
                );
            throwIfErrorStatus(status);
            var branches = jsonParser.fromJson(body, Branch[].class);
            var result = new ArrayList<Branch>(branches.length);
            Collections.addAll(result, branches);
            logger.debug(
                "Found {} branches for repository {}/{}",
                result.size(),
                repositoryOwnerLogin,
                repositoryName
            );
            return result;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpRequest.Builder getRequestBuilder() {
        var builder = HttpRequest.newBuilder()
            .header("Accept", "application/json")
            .header("X-GitHub-Api-Version", "2026-03-10");
        if (this.githubApiToken != null && !this.githubApiToken.isBlank())
            builder = builder.header(
                "Authorization",
                "Bearer " + this.githubApiToken
            );
        return builder;
    }

    private void throwIfErrorStatus(int statusCode)
        throws RateLimitExceededException {

        if (statusCode == 403)
            throw new RateLimitExceededException("Rate limit exceeded.");
        if (statusCode != 200)
            throw new RuntimeException(
             "Unexpected response code: " + statusCode + "."
            );
    }
}
