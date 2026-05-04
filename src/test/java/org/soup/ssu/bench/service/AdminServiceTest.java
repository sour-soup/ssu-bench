package org.soup.ssu.bench.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.soup.ssu.bench.exception.EntityNotFoundException;
import org.soup.ssu.bench.repository.UserRepository;
import org.soup.ssu.bench.repository.entity.UserEntity;
import ssu.bench.model.PageUserResponse;
import ssu.bench.model.UserResponse;
import ssu.bench.model.UserStatusEnum;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.soup.ssu.bench.generator.EntityGenerator.CUSTOMER_ID;
import static org.soup.ssu.bench.generator.EntityGenerator.buildUserEntity;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminService adminService;

    @Test
    void givenExistUser_whenBlockUser_thenSuccess() {
        // given
        UserEntity userEntity = buildUserEntity().withId(CUSTOMER_ID);
        when(userRepository.getUserById(userEntity.id())).thenReturn(Optional.of(userEntity));

        // when
        adminService.blockUser(userEntity.id());

        // then
        verify(userRepository).updateStatus(userEntity.id(), UserStatusEnum.BLOCKED.getValue());
    }

    @Test
    void givenNewUser_whenBlockUser_thenThrowNotFound() {
        // given
        when(userRepository.getUserById(CUSTOMER_ID)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminService.blockUser(CUSTOMER_ID))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void givenExistUser_whenUnblockUser_thenSuccess() {
        // given
        UserEntity userEntity = buildUserEntity().withId(CUSTOMER_ID);
        when(userRepository.getUserById(userEntity.id())).thenReturn(Optional.of(userEntity));

        // when
        adminService.unblockUser(userEntity.id());

        // then
        verify(userRepository).updateStatus(userEntity.id(), UserStatusEnum.ACTIVE.getValue());
    }

    @Test
    void givenNewUser_whenUnblockUser_thenThrowNotFound() {
        // given
        when(userRepository.getUserById(CUSTOMER_ID)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminService.unblockUser(CUSTOMER_ID))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void givenListUser_whenGetListUsers_thenReturnListUsers() {
        // given
        UserEntity userEntity = buildUserEntity().withId(CUSTOMER_ID);
        when(userRepository.getUsers(anyInt(), anyInt())).thenReturn(List.of(userEntity));

        // when
        PageUserResponse result = adminService.getUsers(0, 10);

        // then
        List<UserResponse> users = result.getContent();
        assertThat(users).hasSize(1);

        UserResponse userResponse = users.getFirst();
        assertEquals(userEntity.id(), userResponse.getId());
        assertEquals(userEntity.username(), userResponse.getUsername());
        assertEquals(userEntity.role(), userResponse.getRole().getValue());
        assertEquals(userEntity.status(), userResponse.getStatus().getValue());
        assertEquals(userEntity.balance(), userResponse.getBalance());
    }
}
