package org.soup.ssu.bench.feature.bids.accept;

import org.springframework.jdbc.core.RowMapper;
import ssu.bench.model.BidStatusEnum;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;

class AcceptBidRowMapper implements RowMapper<BidData> {
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TASK_ID = "task_id";
    private static final String COLUMN_EXECUTOR_ID = "executor_id";
    private static final String COLUMN_CUSTOMER_ID = "customer_id";
    private static final String COLUMN_STATUS = "status";
    private static final String COLUMN_CREATED_AT = "created_at";

    @Override
    public BidData mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new BidData(
            rs.getObject(COLUMN_ID, BigInteger.class),
            rs.getObject(COLUMN_TASK_ID, BigInteger.class),
            rs.getObject(COLUMN_EXECUTOR_ID, BigInteger.class),
            BidStatusEnum.fromValue(rs.getString(COLUMN_STATUS)),
            rs.getTimestamp(COLUMN_CREATED_AT).toLocalDateTime(),
            rs.getObject(COLUMN_CUSTOMER_ID, BigInteger.class)
        );
    }
}
