package org.soup.ssu.bench.feature.admin.unblockuser;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ssu.bench.model.UserStatusEnum;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
class UnblockUserRepository {

    private static final String PARAM_USER_ID = "userId";
    private static final String PARAM_STATUS = "status";
    private static final String PARAM_UPDATED_AT = "updatedAt";

    private static final String SQL_COUNT_USER = """
            SELECT count(1) FROM users WHERE id = :userId
        """;

    private static final String SQL_UNBLOCK_USER = """
            UPDATE users SET status = :status, updated_at = :updatedAt WHERE id = :userId
        """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public void unblockUser(BigInteger userId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue(PARAM_USER_ID, userId)
            .addValue(PARAM_STATUS, UserStatusEnum.ACTIVE.name())
            .addValue(PARAM_UPDATED_AT, Timestamp.valueOf(LocalDateTime.now()));

        jdbcTemplate.update(SQL_UNBLOCK_USER, params);
    }

    public boolean userExists(BigInteger userId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue(PARAM_USER_ID, userId);

        Integer count = jdbcTemplate.queryForObject(SQL_COUNT_USER, params, Integer.class);
        return count != null && count > 0;
    }
}
