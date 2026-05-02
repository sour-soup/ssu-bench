package org.soup.ssu.bench.feature.admin.unblockuser;

import lombok.RequiredArgsConstructor;
import org.soup.ssu.bench.exception.EntityNotFoundException;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

@Component
@RequiredArgsConstructor
public class UnblockUserUseCase {

    private final UnblockUserRepository repository;

    public void execute(BigInteger userId) {
        if (!repository.userExists(userId)) {
            throw new EntityNotFoundException("User", userId);
        }

        repository.unblockUser(userId);
    }
}
