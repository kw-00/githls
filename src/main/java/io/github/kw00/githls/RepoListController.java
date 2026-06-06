package io.github.kw00.githls;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RepoListController {

    private final RepoListService repoListService;

    @Autowired
    public RepoListController(RepoListService repoListService) {
        this.repoListService = repoListService;
    }

    @GetMapping("/{userLogin}")
    public List<RepositoryDto> getRepositories(
        @PathVariable String userLogin
    ) throws UserNotFoundException, RateLimitExceededException {
        return repoListService.getRepositories(userLogin);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFound(
        UserNotFoundException ex
    ) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponseDto("GITHUB_USER_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponseDto> handleRateLimit(
        RateLimitExceededException ex
    ) {
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(new ErrorResponseDto(
                "SERVICE_OVERLOADED", "Service unavailable. Please try again later"
            ));
    }    
}
