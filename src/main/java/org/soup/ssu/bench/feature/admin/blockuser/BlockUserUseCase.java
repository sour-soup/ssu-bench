package org.soup.ssu.bench.feature.admin.blockuser;

import lombok.RequiredArgsConstructor;
import org.soup.ssu.bench.exception.EntityNotFoundException;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

@Component
@RequiredArgsConstructor
public class BlockUserUseCase {

    private final BlockUserRepository repository;

    public void execute(BigInteger userId) {
        if (!repository.userExists(userId)) {
            throw new EntityNotFoundException("User", userId);
        }

        repository.blockUser(userId);
    }
}
