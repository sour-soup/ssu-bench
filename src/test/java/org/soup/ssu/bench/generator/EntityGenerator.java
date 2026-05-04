package org.soup.ssu.bench.generator;

import lombok.experimental.UtilityClass;
import org.soup.ssu.bench.repository.entity.BidEntity;
import org.soup.ssu.bench.repository.entity.PaymentEntity;
import org.soup.ssu.bench.repository.entity.TaskEntity;
import org.soup.ssu.bench.repository.entity.UserEntity;
import ssu.bench.model.BidStatusEnum;
import ssu.bench.model.RoleEnum;
import ssu.bench.model.TaskStatusEnum;
import ssu.bench.model.UserStatusEnum;

import java.math.BigInteger;
import java.time.LocalDateTime;

@UtilityClass
public class EntityGenerator {

    public static final BigInteger CUSTOMER_ID = BigInteger.ONE;
    public static final BigInteger EXECUTOR_ID = BigInteger.TWO;
    public static final BigInteger TASK_ID = BigInteger.TWO;
    public static final String USERNAME = "soup";
    public static final String PASSWORD = "password";
    public static final String TOKEN = "jwt-token-123";
    public static final String PASSWORD_HASH = "$2a$10$hashedPassword";
    public static final String HOLD_TYPE = "HOLD";

    public static UserEntity buildUserEntity() {
        return UserEntity.builder()
            .username(USERNAME)
            .passwordHash(PASSWORD_HASH)
            .balance(BigInteger.ZERO)
            .role(RoleEnum.CUSTOMER.getValue())
            .status(UserStatusEnum.ACTIVE.getValue())
            .build();
    }

    public static BidEntity buildBidEntity() {
        return BidEntity.builder()
            .status(BidStatusEnum.PENDING.getValue())
            .taskId(TASK_ID)
            .executorId(EXECUTOR_ID)
            .createdAt(LocalDateTime.now())
            .build();
    }

    public static PaymentEntity buildPaymentEntity() {
        return PaymentEntity.builder()
            .type(HOLD_TYPE)
            .taskId(TASK_ID)
            .senderId(CUSTOMER_ID)
            .receiverId(EXECUTOR_ID)
            .build();
    }

    public static TaskEntity buildTaskEntity() {
        return TaskEntity.builder()
            .title("title")
            .description("description")
            .reward(BigInteger.ZERO)
            .status(TaskStatusEnum.PUBLISHED.getValue())
            .customerId(CUSTOMER_ID)
            .build();
    }
}
