package io.github.kw00.githls;

public class RepositoryNotFoundException extends Exception {
    public RepositoryNotFoundException(String message) {
        super(message);
    }
}
