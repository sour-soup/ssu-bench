package org.soup.ssu.bench.feature.admin.listusers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ssu.bench.model.PageUserResponse;
import ssu.bench.model.RoleEnum;
import ssu.bench.model.UserResponse;
import ssu.bench.model.UserStatusEnum;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ListUsersUseCaseTest {

    @Mock
    private ListUsersRepository repository;

    @InjectMocks
    private ListUsersUseCase listUsersUseCase;

    private Integer page;
    private Integer size;
    private List<UserResponse> testUsers;

    @BeforeEach
    void setUp() {
        page = 0;
        size = 20;

        UserResponse user1 = new UserResponse()
            .id(BigInteger.valueOf(1L))
            .username("user1")
            .role(RoleEnum.CUSTOMER)
            .balance(BigInteger.valueOf(100L))
            .status(UserStatusEnum.ACTIVE)
            .createdAt(LocalDateTime.now());

        UserResponse user2 = new UserResponse()
            .id(BigInteger.valueOf(2L))
            .username("user2")
            .role(RoleEnum.EXECUTOR)
            .balance(BigInteger.valueOf(200L))
            .status(UserStatusEnum.ACTIVE)
            .createdAt(LocalDateTime.now().minusDays(1));

        testUsers = List.of(user1, user2);
    }

    @Test
    @DisplayName("Успешное получение списка пользователей")
    void shouldReturnPageUserResponse() {
        // Given
        given(repository.getUsers(page, size)).willReturn(testUsers);

        // When
        PageUserResponse response = listUsersUseCase.execute(page, size);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getPage()).isEqualTo(page);
        assertThat(response.getSize()).isEqualTo(size);
        assertThat(response.getContent()).hasSize(2);
        verify(repository).getUsers(page, size);
    }

    @Test
    @DisplayName("Получение пустого списка пользователей")
    void shouldReturnEmptyListWhenNoUsers() {
        // Given
        given(repository.getUsers(page, size)).willReturn(List.of());

        // When
        PageUserResponse response = listUsersUseCase.execute(page, size);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getPage()).isEqualTo(page);
        assertThat(response.getSize()).isEqualTo(size);
        assertThat(response.getContent()).isEmpty();
        verify(repository).getUsers(page, size);
    }

    @Test
    @DisplayName("Получение пользователей с другой страницей")
    void shouldReturnUsersForDifferentPage() {
        // Given
        Integer differentPage = 5;
        Integer differentSize = 10;
        given(repository.getUsers(differentPage, differentSize)).willReturn(List.of());

        // When
        PageUserResponse response = listUsersUseCase.execute(differentPage, differentSize);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getPage()).isEqualTo(differentPage);
        assertThat(response.getSize()).isEqualTo(differentSize);
        verify(repository).getUsers(differentPage, differentSize);
    }

    @Test
    @DisplayName("Получение одного пользователя")
    void shouldReturnSingleUser() {
        // Given
        UserResponse singleUser = new UserResponse()
            .id(BigInteger.valueOf(99L))
            .username("single_user")
            .role(RoleEnum.ADMIN)
            .balance(BigInteger.valueOf(500L))
            .status(UserStatusEnum.BLOCKED)
            .createdAt(LocalDateTime.now());
        given(repository.getUsers(page, size)).willReturn(List.of(singleUser));

        // When
        PageUserResponse response = listUsersUseCase.execute(page, size);

        // Then
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().getFirst().getId()).isEqualTo(BigInteger.valueOf(99L));
        assertThat(response.getContent().getFirst().getUsername()).isEqualTo("single_user");
    }
}
