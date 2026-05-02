package org.soup.ssu.bench.feature.auth.login;

import org.springframework.jdbc.core.RowMapper;
import ssu.bench.model.RoleEnum;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;

class LoginCredentialsRowMapper implements RowMapper<LoginCredentials> {
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_PASSWORD_HASH = "password_hash";
    private static final String COLUMN_ROLE = "role";

    @Override
    public LoginCredentials mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new LoginCredentials(
            rs.getObject(COLUMN_ID, BigInteger.class),
            rs.getString(COLUMN_PASSWORD_HASH),
            RoleEnum.fromValue(rs.getString(COLUMN_ROLE))
        );
    }
}
