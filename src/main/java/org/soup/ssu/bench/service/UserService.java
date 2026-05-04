package org.soup.ssu.bench.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.soup.ssu.bench.exception.EntityNotFoundException;
import org.soup.ssu.bench.repository.UserRepository;
import org.soup.ssu.bench.repository.entity.UserEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ssu.bench.model.RoleEnum;
import ssu.bench.model.UserResponse;
import ssu.bench.model.UserStatusEnum;

import java.math.BigInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserResponse getUser(BigInteger userId) {
        UserEntity userEntity = userRepository.getUserById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User", userId));

        return mapUserEntityToResponse(userEntity);
    }

    @Transactional
    public UserResponse deposit(BigInteger userId, BigInteger amount) {
        UserEntity userEntity = userRepository.getUserById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User", userId));

        UserEntity updatedUser = userRepository.updateBalance(userId, userEntity.balance().add(amount));

        return mapUserEntityToResponse(updatedUser);
    }

    private UserResponse mapUserEntityToResponse(UserEntity userEntity) {
        return new UserResponse()
            .id(userEntity.id())
            .username(userEntity.username())
            .role(RoleEnum.fromValue(userEntity.role()))
            .balance(userEntity.balance())
            .status(UserStatusEnum.fromValue(userEntity.status()))
            .createdAt(userEntity.createdAt());
    }
}
