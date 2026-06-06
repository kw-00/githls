package io.github.kw00.githls;

import java.util.Objects;

public record Branch(String name, Commit commit) {
    public Branch {
        Objects.requireNonNull(name, "Parameter \"name\" is required.");
        Objects.requireNonNull(commit, "Parameter \"commit\" is required.");
    }
}
