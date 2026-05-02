package org.soup.ssu.bench.feature.auth.login;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
class LoginRepository {
    private static final String PARAM_USERNAME = "username";

    private static final String SQL_FIND_CREDENTIALS = """
            SELECT id, password_hash, role FROM users WHERE username = :username
        """;
    private static final LoginCredentialsRowMapper ROW_MAPPER = new LoginCredentialsRowMapper();

    private final NamedParameterJdbcTemplate jdbcTemplate;

    Optional<LoginCredentials> findCredentialsByUsername(String username) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue(PARAM_USERNAME, username);

        return jdbcTemplate.query(SQL_FIND_CREDENTIALS, params, ROW_MAPPER)
            .stream()
            .findFirst();
    }
}
