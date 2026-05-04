package org.soup.ssu.bench.repository.entity;

import lombok.Builder;
import lombok.With;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigInteger;

import static org.soup.ssu.bench.constant.CommonDataConstants.ID_COL;

@With
@Builder
public record PaymentEntity(BigInteger id,
                            BigInteger taskId,
                            BigInteger senderId,
                            BigInteger receiverId,
                            String type) {

    public static final String TASK_ID_COL = "task_id";
    public static final String SENDER_ID_COL = "sender_id";
    public static final String RECEIVER_ID_COL = "receiver_id";
    public static final String TYPE_COL = "type";

    public static final RowMapper<PaymentEntity> PAYMENT_ROW_MAPPER = (rs, rowNum) -> PaymentEntity.builder()
        .id(BigInteger.valueOf(rs.getLong(ID_COL)))
        .taskId(BigInteger.valueOf(rs.getLong(TASK_ID_COL)))
        .senderId(BigInteger.valueOf(rs.getLong(SENDER_ID_COL)))
        .receiverId(BigInteger.valueOf(rs.getLong(RECEIVER_ID_COL)))
        .type(rs.getString(TYPE_COL))
        .build();
}
