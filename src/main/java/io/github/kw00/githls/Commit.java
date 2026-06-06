package io.github.kw00.githls;

import java.util.Objects;

public record Commit(String sha) {
    public Commit {
        Objects.requireNonNull(sha, "Parameter \"sha\" is required.");
    }
}
