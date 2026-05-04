package org.soup.ssu.bench.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.soup.ssu.bench.exception.EntityNotFoundException;
import org.soup.ssu.bench.repository.UserRepository;
import org.soup.ssu.bench.repository.entity.UserEntity;
import org.springframework.stereotype.Service;
import ssu.bench.model.PageUserResponse;
import ssu.bench.model.RoleEnum;
import ssu.bench.model.UserResponse;
import ssu.bench.model.UserStatusEnum;

import java.math.BigInteger;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;

    public void blockUser(BigInteger userId) {
        if (userRepository.getUserById(userId).isEmpty()) {
            throw new EntityNotFoundException("User", userId);
        }

        userRepository.updateStatus(userId, UserStatusEnum.BLOCKED.getValue());
    }

    public void unblockUser(BigInteger userId) {
        if (userRepository.getUserById(userId).isEmpty()) {
            throw new EntityNotFoundException("User", userId);
        }

        userRepository.updateStatus(userId, UserStatusEnum.ACTIVE.getValue());
    }

    public PageUserResponse getUsers(int page, int size) {
        List<UserResponse> users = userRepository.getUsers(page, size).stream()
            .map(this::mapUserEntityToResponse)
            .toList();

        return new PageUserResponse()
            .page(page)
            .size(size)
            .content(users);
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
