package io.github.kw00.githls;

import java.util.Objects;

public record Repository(
    String name,
    RepoOwner owner,
    boolean fork) {

    public Repository {
        Objects.requireNonNull("Parameter \"name\" is required.");
        Objects.requireNonNull("Parameter \"owner\" is required.");
        Objects.requireNonNull("Parameter \"fork\" is required.");
    }
}
