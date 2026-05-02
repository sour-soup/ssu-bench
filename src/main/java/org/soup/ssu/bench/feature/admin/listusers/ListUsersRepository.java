package org.soup.ssu.bench.feature.admin.listusers;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ssu.bench.model.UserResponse;

import java.util.List;

@Repository
@RequiredArgsConstructor
class ListUsersRepository {

    private static final String SQL_GET_USERS = """
            SELECT id, username, role, balance, status, created_at, updated_at
            FROM users ORDER BY created_at DESC LIMIT :limit OFFSET :offset
        """;
    private static final RowMapper<UserResponse> USER_ROW_MAPPER = new UserRowMapper();

    private static final String PARAM_LIMIT = "limit";
    private static final String PARAM_OFFSET = "offset";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public List<UserResponse> getUsers(Integer page, Integer size) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue(PARAM_LIMIT, size)
            .addValue(PARAM_OFFSET, page * size);

        return jdbcTemplate.query(SQL_GET_USERS, params, USER_ROW_MAPPER);
    }
}
