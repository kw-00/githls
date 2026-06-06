package io.github.kw00.githls;

import java.util.Objects;

public class BranchDto {
    public final String name;
    public final String lastCommitSha;

    public BranchDto(Branch branch) {
        Objects.requireNonNull(branch, "Parameter \"branch\" is required.");
        this.name = branch.name();
        this.lastCommitSha = branch.commit().sha();
    }
}
