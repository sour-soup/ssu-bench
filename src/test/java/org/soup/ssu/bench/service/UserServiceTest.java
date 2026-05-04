package org.soup.ssu.bench.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.soup.ssu.bench.exception.EntityNotFoundException;
import org.soup.ssu.bench.repository.UserRepository;
import org.soup.ssu.bench.repository.entity.UserEntity;
import ssu.bench.model.UserResponse;

import java.math.BigInteger;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.soup.ssu.bench.generator.EntityGenerator.CUSTOMER_ID;
import static org.soup.ssu.bench.generator.EntityGenerator.buildUserEntity;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void givenExistUser_whenGetUserById_thenReturnUser() {
        // given
        UserEntity userEntity = buildUserEntity().withId(CUSTOMER_ID);
        when(userRepository.getUserById(userEntity.id())).thenReturn(Optional.of(userEntity));

        // when
        UserResponse userResponse = userService.getUser(userEntity.id());

        // then
        assertUserMapping(userEntity, userResponse);
    }

    @Test
    void givenUnknownUser_whenGetUserById_thenThrowNotFound() {
        // given
        when(userRepository.getUserById(CUSTOMER_ID)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getUser(CUSTOMER_ID))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void givenExistUser_whenDeposit_thenReturnUpdatedUser() {
        // given
        BigInteger amount = BigInteger.valueOf(1000);
        UserEntity userEntity = buildUserEntity().withId(CUSTOMER_ID);
        when(userRepository.getUserById(userEntity.id())).thenReturn(Optional.of(userEntity));
        when(userRepository.updateBalance(userEntity.id(), userEntity.balance().add(amount)))
            .thenReturn(userEntity.withBalance(userEntity.balance().add(amount)));

        // when
        UserResponse userResponse = userService.deposit(userEntity.id(), amount);

        // then
        assertUserMapping(userEntity.withBalance(userEntity.balance().add(amount)), userResponse);
        verify(userRepository).updateBalance(userEntity.id(), userEntity.balance().add(amount));
    }

    @Test
    void givenUnknownUser_whenDeposit_thenThrowNotFound() {
        // given
        when(userRepository.getUserById(CUSTOMER_ID)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.deposit(CUSTOMER_ID, BigInteger.valueOf(1000)))
            .isInstanceOf(EntityNotFoundException.class);
    }

    private static void assertUserMapping(UserEntity userEntity, UserResponse userResponse) {
        assertEquals(userEntity.id(), userResponse.getId());
        assertEquals(userEntity.username(), userResponse.getUsername());
        assertEquals(userEntity.role(), userResponse.getRole().getValue());
        assertEquals(userEntity.balance(), userResponse.getBalance());
        assertEquals(userEntity.status(), userResponse.getStatus().getValue());
        assertEquals(userEntity.createdAt(), userResponse.getCreatedAt());
    }

}
