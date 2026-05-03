package org.soup.ssu.bench.repository;

import org.junit.jupiter.api.Test;
import org.soup.ssu.bench.RepositoryTest;
import org.soup.ssu.bench.repository.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Import(UserRepository.class)
class UserRepositoryTest extends RepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void givenUser_whenCreateUser_thenReturnUserWithId() {
        // given
        UserEntity userEntity = buildUserEntity();

        // when
        UserEntity insertedUser = userRepository.createUser(userEntity);

        // then
        assertNotNull(insertedUser.id());
        assertEquals(userEntity.withId(insertedUser.id()), insertedUser);
    }

    @Test
    void givenUserInDb_whenUpdateBalance_thenReturnUpdatedUser() {
        // given
        BigInteger newBalance = BigInteger.valueOf(1334);
        UserEntity userEntity = userRepository.createUser(buildUserEntity());

        // when
        UserEntity insertedUser = userRepository.updateBalance(userEntity.id(), newBalance);

        // then
        assertEquals(userEntity.withBalance(newBalance), insertedUser);
    }

    @Test
    void givenUserInDb_whenUpdateStatus_thenReturnUpdatedUser() {
        // given
        String newStatus = "blocked";
        UserEntity userEntity = userRepository.createUser(buildUserEntity());

        // when
        UserEntity insertedUser = userRepository.updateStatus(userEntity.id(), newStatus);

        // then
        assertEquals(userEntity.withStatus(newStatus), insertedUser);
    }

    @Test
    void givenUserInDb_whenGetUserById_thenReturnUser() {
        // given
        UserEntity userEntity = userRepository.createUser(buildUserEntity());

        // when
        Optional<UserEntity> result = userRepository.getUserById(userEntity.id());

        // then
        assertThat(result).get().isEqualTo(userEntity);
    }

    @Test
    void givenEmptyDb_whenGetUserByUd_thenReturnEmpty() {
        // when
        Optional<UserEntity> result = userRepository.getUserById(BigInteger.valueOf(999));

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void givenUserInDb_whenGetUsers_thenReturnUser() {
        // given
        UserEntity userEntity = userRepository.createUser(buildUserEntity());

        // when
        List<UserEntity> result = userRepository.getUsers(0, 10);

        // then
        assertThat(result).containsExactly(userEntity);
    }

    @Test
    void givenUsersInDb_whenGetUsers_thenReturnLimitUsers() {
        // given
        userRepository.createUser(buildUserEntity().withUsername("1"));
        userRepository.createUser(buildUserEntity().withUsername("2"));
        userRepository.createUser(buildUserEntity().withUsername("3"));

        // when
        List<UserEntity> result = userRepository.getUsers(0, 2);

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    void givenUsersInDb_whenGetUsers_thenReturnOffsetUsers() {
        // given
        userRepository.createUser(buildUserEntity().withUsername("1"));
        userRepository.createUser(buildUserEntity().withUsername("2"));
        userRepository.createUser(buildUserEntity().withUsername("3"));

        // when
        List<UserEntity> result = userRepository.getUsers(1, 2);

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    void givenEmptyDb_whenGetUsers_thenReturnEmptyList() {
        // when
        List<UserEntity> result = userRepository.getUsers(0, 1);

        // then
        assertThat(result).isEmpty();
    }

    private static UserEntity buildUserEntity() {
        return UserEntity.builder()
            .username("username")
            .passwordHash("password_hash")
            .role("role")
            .balance(BigInteger.valueOf(137))
            .status("status")
            .build();
    }
}
