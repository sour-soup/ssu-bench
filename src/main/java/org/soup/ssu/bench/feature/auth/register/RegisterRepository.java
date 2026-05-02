package org.soup.ssu.bench.feature.auth.register;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.soup.ssu.bench.exception.InternalErrorException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ssu.bench.model.RoleEnum;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RegisterRepository {

    private static final String PARAM_USERNAME = "username";
    private static final String PARAM_PASSWORD_HASH = "passwordHash";
    private static final String PARAM_ROLE = "role";
    private static final String PARAM_CREATED_AT = "createdAt";
    private static final String PARAM_UPDATED_AT = "updatedAt";

    private static final String SQL_CREATE_USER = """
            INSERT INTO users (username, password_hash, role, balance, status, created_at, updated_at)
            VALUES (:username, :passwordHash, :role, 0, 'ACTIVE', :createdAt, :updatedAt)
            RETURNING id
        """;

    private static final String SQL_EXISTS_BY_USERNAME = """
            SELECT 1 FROM users WHERE username = :username
        """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public BigInteger createUser(String username, String passwordHash, RoleEnum role) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue(PARAM_USERNAME, username)
            .addValue(PARAM_PASSWORD_HASH, passwordHash)
            .addValue(PARAM_ROLE, role.name())
            .addValue(PARAM_CREATED_AT, Timestamp.valueOf(LocalDateTime.now()))
            .addValue(PARAM_UPDATED_AT, Timestamp.valueOf(LocalDateTime.now()));

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(SQL_CREATE_USER, params, keyHolder);

        return Optional.ofNullable(keyHolder.getKey())
            .map(Number::longValue)
            .map(BigInteger::valueOf)
            .orElseThrow(() -> new InternalErrorException("Create user error"));
    }

    public boolean existsByUsername(String username) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue(PARAM_USERNAME, username);

        return !jdbcTemplate.queryForList(SQL_EXISTS_BY_USERNAME, params, Integer.class).isEmpty();
    }
}
