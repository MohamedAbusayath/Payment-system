package com.payment.IntegrationTesting;

import com.payment.entity.Payment;
import com.payment.enums.PaymentStatus;
import com.payment.repository.PaymentRepo;
import com.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class PaymentServiceIntegrationTesting {
    @Autowired
    PaymentService ser;
    @Autowired
    PaymentRepo repo;

    @Test
    public void approvePay(){
        Payment pay=new Payment();
        pay.setAmount(1500);
        pay.setPaymentType("CARD");
        pay.setStatus(PaymentStatus.PENDING_APPROVAL);

        pay=repo.save(pay);

        Payment checker = ser.approvePayment(pay.getId(), "CHECKER");

        assertEquals(PaymentStatus.APPROVED,checker.getStatus());

    }
}
