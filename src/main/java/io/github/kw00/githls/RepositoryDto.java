package io.github.kw00.githls;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class RepositoryDto {
    public final String name;
    public final String ownerLogin;
    public final List<BranchDto> branches;

    public RepositoryDto(Repository repository, Collection<Branch> branches) {
        Objects.requireNonNull(repository, "Parameter \"repository\" is required.");
        Objects.requireNonNull(branches, "Parameter \"branches\" is required.");
        this.name = repository.name();
        this.ownerLogin = repository.owner().login();
        this.branches = branches.stream()
            .map(branch -> new BranchDto(branch))
            .toList();
    }
}
