package org.soup.ssu.bench.feature.admin.listusers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ssu.bench.model.PageUserResponse;
import ssu.bench.model.UserResponse;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ListUsersUseCase {

    private final ListUsersRepository repository;

    public PageUserResponse execute(Integer page, Integer size) {
        List<UserResponse> users = repository.getUsers(page, size);

        return new PageUserResponse()
            .page(page)
            .size(size)
            .content(users);
    }
}
