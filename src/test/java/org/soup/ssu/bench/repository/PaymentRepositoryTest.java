package org.soup.ssu.bench.repository;

import org.junit.jupiter.api.Test;
import org.soup.ssu.bench.RepositoryTest;
import org.soup.ssu.bench.repository.entity.PaymentEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.soup.ssu.bench.generator.EntityGenerator.buildPaymentEntity;

@Import(PaymentRepository.class)
public class PaymentRepositoryTest extends RepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    void givenPayment_whenCreatePayment_thenReturnPaymentWithId() {
        // given
        PaymentEntity paymentEntity = buildPaymentEntity();

        // when
        PaymentEntity insertedPayment = paymentRepository.createPayment(paymentEntity);

        // then
        assertNotNull(insertedPayment.id());
        assertEquals(paymentEntity.withId(insertedPayment.id()), insertedPayment);
    }
}
