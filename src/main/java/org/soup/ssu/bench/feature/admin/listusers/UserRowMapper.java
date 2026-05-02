package org.soup.ssu.bench.feature.admin.listusers;

import org.springframework.jdbc.core.RowMapper;
import ssu.bench.model.RoleEnum;
import ssu.bench.model.UserResponse;
import ssu.bench.model.UserStatusEnum;

import java.math.BigInteger;
import java.sql.ResultSet;

class UserRowMapper implements RowMapper<UserResponse> {

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_ROLE = "role";
    private static final String COLUMN_BALANCE = "balance";
    private static final String COLUMN_STATUS = "status";
    private static final String COLUMN_CREATED_AT = "created_at";

    @Override
    public UserResponse mapRow(ResultSet rs, int rowNum) throws java.sql.SQLException {
        UserResponse response = new UserResponse();
        response.setId(rs.getObject(COLUMN_ID, BigInteger.class));
        response.setUsername(rs.getString(COLUMN_USERNAME));
        response.setRole(RoleEnum.fromValue(rs.getString(COLUMN_ROLE)));
        response.setBalance(rs.getObject(COLUMN_BALANCE, BigInteger.class));
        response.setStatus(UserStatusEnum.fromValue(rs.getString(COLUMN_STATUS)));
        response.setCreatedAt(rs.getTimestamp(COLUMN_CREATED_AT).toLocalDateTime());
        return response;
    }
}
