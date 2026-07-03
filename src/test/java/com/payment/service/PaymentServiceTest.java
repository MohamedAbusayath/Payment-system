package com.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.entity.Payment;
import com.payment.enums.PaymentStatus;
import com.payment.notification.PaymentEventProducer;
import com.payment.repository.AuditLogRepository;
import com.payment.repository.FailedTransactionRepository;
import com.payment.repository.PaymentRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepo repo;
    @Mock
    private RestTemplate template;
    @Mock
    private FailedTransactionRepository failedRepo;
    @Mock
    private AuditLogRepository auditRepo;
    @Mock
    private ObjectMapper objMap;
    @Mock
    private PaymentEventProducer producer;
    @InjectMocks
    private PaymentService ser;

    @Test
    void approvePaymentSuccessfully() {

        Payment payment = new Payment();

        payment.setId(1L);
        payment.setAmount(1000);
        payment.setPaymentType("CARD");
        payment.setStatus(PaymentStatus.PENDING_APPROVAL);

        when(repo.findById(1L))
                .thenReturn(Optional.of(payment));

        when(repo.save(any(Payment.class)))
                .thenReturn(payment);

        Payment result =
                ser.approvePayment(
                        1L,
                        "checker");

        assertEquals(
                PaymentStatus.APPROVED,
                result.getStatus());

        assertEquals(
                "Approved By Checker",
                result.getMessage());

        verify(repo)
                .save(any(Payment.class));

        verify(auditRepo)
                .save(any());

        verify(producer)
                .publishPayEvent(any());
    }
}
