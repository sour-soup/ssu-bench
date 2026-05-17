package org.soup.ssu.bench.repository;

import lombok.RequiredArgsConstructor;
import org.soup.ssu.bench.repository.entity.PaymentEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import static org.soup.ssu.bench.repository.entity.PaymentEntity.AMOUNT_COL;
import static org.soup.ssu.bench.repository.entity.PaymentEntity.PAYMENT_ROW_MAPPER;
import static org.soup.ssu.bench.repository.entity.PaymentEntity.RECEIVER_ID_COL;
import static org.soup.ssu.bench.repository.entity.PaymentEntity.SENDER_ID_COL;
import static org.soup.ssu.bench.repository.entity.PaymentEntity.TASK_ID_COL;
import static org.soup.ssu.bench.repository.entity.PaymentEntity.TYPE_COL;

@Repository
@RequiredArgsConstructor
public class PaymentRepository {

    private static final String SQL_CREATE_PAYMENT = """
            INSERT INTO payments (task_id, amount, type, sender_id, receiver_id, created_at, updated_at)
            VALUES(:task_id, :amount, :type, :sender_id, :receiver_id, now(), now())
            RETURNING id, amount, task_id, type, sender_id, receiver_id;
        """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public PaymentEntity createPayment(PaymentEntity paymentEntity) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue(TASK_ID_COL, paymentEntity.taskId())
            .addValue(AMOUNT_COL, paymentEntity.amount())
            .addValue(TYPE_COL, paymentEntity.type())
            .addValue(SENDER_ID_COL, paymentEntity.senderId())
            .addValue(RECEIVER_ID_COL, paymentEntity.receiverId());

        return jdbcTemplate.queryForObject(SQL_CREATE_PAYMENT, params, PAYMENT_ROW_MAPPER);
    }
}
