package org.soup.ssu.bench.repository;

import lombok.RequiredArgsConstructor;
import org.soup.ssu.bench.repository.entity.UserEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static org.soup.ssu.bench.constant.CommonDataConstants.ID_COL;
import static org.soup.ssu.bench.constant.CommonDataConstants.LIMIT_PARAM;
import static org.soup.ssu.bench.constant.CommonDataConstants.OFFSET_PARAM;
import static org.soup.ssu.bench.repository.entity.UserEntity.BALANCE_COL;
import static org.soup.ssu.bench.repository.entity.UserEntity.PASSWORD_HASH_COL;
import static org.soup.ssu.bench.repository.entity.UserEntity.ROLE_COL;
import static org.soup.ssu.bench.repository.entity.UserEntity.USER_ROW_MAPPER;
import static org.soup.ssu.bench.repository.entity.UserEntity.STATUS_COL;
import static org.soup.ssu.bench.repository.entity.UserEntity.USERNAME_COL;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private static final String SQL_CREATE_USER = """
            INSERT INTO users (username, password_hash, role, balance, status, created_at, updated_at)
            VALUES (:username, :password_hash, :role, :balance, :status, now(), now())
            RETURNING id, username, password_hash, role, balance, status
        """;

    private static final String SQL_UPDATE_BALANCE = """
            UPDATE users SET balance = :balance, updated_at = now()
            WHERE id = :id
            RETURNING id, username, password_hash, role, balance, status
        """;

    private static final String SQL_UPDATE_STATUS = """
            UPDATE users SET status = :status, updated_at = now()
            WHERE id = :id
            RETURNING id, username, password_hash, role, balance, status
        """;

    private static final String SQL_GET_USER = """
            SELECT id, username, password_hash, role, balance, status FROM users
            WHERE id = :id
        """;

    private static final String SQL_GET_USERS = """
            SELECT id, username, password_hash, role, balance, status FROM users
            ORDER BY created_at DESC LIMIT :limit OFFSET :offset
        """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public UserEntity createUser(UserEntity userEntity) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue(USERNAME_COL, userEntity.username())
            .addValue(PASSWORD_HASH_COL, userEntity.passwordHash())
            .addValue(ROLE_COL, userEntity.role())
            .addValue(BALANCE_COL, userEntity.balance())
            .addValue(STATUS_COL, userEntity.status());

        return jdbcTemplate.query(SQL_CREATE_USER, params, USER_ROW_MAPPER)
            .getFirst();
    }

    public UserEntity updateBalance(BigInteger id, int balance) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue(ID_COL, id)
            .addValue(BALANCE_COL, balance);

        return jdbcTemplate.query(SQL_UPDATE_BALANCE, params, USER_ROW_MAPPER)
            .getFirst();
    }

    public UserEntity updateStatus(BigInteger id, String status) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue(ID_COL, id)
            .addValue(STATUS_COL, status);

        return jdbcTemplate.query(SQL_UPDATE_STATUS, params, USER_ROW_MAPPER)
            .getFirst();
    }

    public Optional<UserEntity> getUserById(BigInteger id) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue(ID_COL, id);

        return jdbcTemplate.query(SQL_GET_USER, params, USER_ROW_MAPPER).stream()
            .findFirst();
    }

    public List<UserEntity> getUsers(int page, int size) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue(LIMIT_PARAM, size)
            .addValue(OFFSET_PARAM, page * size);

        return jdbcTemplate.query(SQL_GET_USERS, params, USER_ROW_MAPPER);
    }
}
