package org.soup.ssu.bench.repository.entity;

import lombok.Builder;
import lombok.With;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigInteger;
import java.time.LocalDateTime;

import static org.soup.ssu.bench.constant.CommonDataConstants.CREATED_AT_COL;
import static org.soup.ssu.bench.constant.CommonDataConstants.ID_COL;
import static org.soup.ssu.bench.constant.CommonDataConstants.STATUS_COL;

@With
@Builder
public record UserEntity(BigInteger id,
                         String username,
                         String passwordHash,
                         String role,
                         BigInteger balance,
                         String status,
                         LocalDateTime createdAt) {

    public static final String USERNAME_COL = "username";
    public static final String PASSWORD_HASH_COL = "password_hash";
    public static final String ROLE_COL = "role";
    public static final String BALANCE_COL = "balance";

    public static final RowMapper<UserEntity> USER_ROW_MAPPER = (rs, rowNum) -> UserEntity.builder()
        .id(BigInteger.valueOf(rs.getLong(ID_COL)))
        .username(rs.getString(USERNAME_COL))
        .passwordHash(rs.getString(PASSWORD_HASH_COL))
        .role(rs.getString(ROLE_COL))
        .balance(BigInteger.valueOf(rs.getLong(BALANCE_COL)))
        .status(rs.getString(STATUS_COL))
        .createdAt(rs.getObject(CREATED_AT_COL, LocalDateTime.class))
        .build();
}
