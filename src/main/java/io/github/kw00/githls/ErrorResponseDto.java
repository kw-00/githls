package io.github.kw00.githls;

import java.util.Objects;

public class ErrorResponseDto {
    public final String status;
    public final String message;

    public ErrorResponseDto(String status, String message) {
        Objects.requireNonNull(status, "Parameter \"status\" is required.");
        Objects.requireNonNull(message, "Parameter \"message\" is required.");
        this.status = status;
        this.message = message;
    }
}
