package com.payment.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.dto.FraudRequest;
import com.payment.dto.FraudResponse;
import com.payment.entity.AuditLog;
import com.payment.entity.FailedTransaction;
import com.payment.entity.Payment;
import com.payment.enums.PaymentStatus;
import com.payment.enums.RetryStatus;
import com.payment.repository.AuditLogRepository;
import com.payment.repository.FailedTransactionRepository;
import com.payment.repository.PaymentRepo;

@Component
public class RetrySchedulerService {

    @Autowired
    private FailedTransactionRepository failedRepo;

    @Autowired
    private PaymentRepo paymentRepo;

    @Autowired
    private AuditLogRepository auditRepo;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String FRAUD_URL =
            "http://localhost:8081/fraud/check";

    @Scheduled(fixedRate = 30000) // Every 5 Minutes
    public void retryFailedTransactions() {

        System.out.println("Retry Scheduler Running...");

        List<FailedTransaction> transactions =
                failedRepo.findByStatus(RetryStatus.PENDING);

        for (FailedTransaction f : transactions) {

            try {

                
                if (f.getRetryCount() >= f.getMaxRetryCount()) {

                    Payment pay = paymentRepo.findById(f.getTransactionId())
                            .orElseThrow(() ->
                                    new RuntimeException("Payment Not Found"));

                    pay.setStatus(PaymentStatus.FAILED);
                    pay.setMessage("Maximum Retry Count Reached");

                    paymentRepo.save(pay);

                    f.setStatus(RetryStatus.FAILED);
                    f.setUpdatedAt(LocalDateTime.now());

                    failedRepo.save(f);

                    saveAudit(pay, "MAX RETRY REACHED");

                    continue;
                }

                FraudRequest fraudRequest =
                        objectMapper.readValue(
                                f.getRequest(),
                                FraudRequest.class
                        );

                FraudResponse fraudResponse =
                        restTemplate.postForObject(
                                FRAUD_URL,
                                fraudRequest,
                                FraudResponse.class
                        );

              
                Payment pay =
                        paymentRepo.findById(f.getTransactionId())
                                .orElseThrow(() ->
                                        new RuntimeException("Payment Not Found"));

                
                if ("SAFE".equals(fraudResponse.getStatus())) {

                    if (pay.getAmount() <= 1000) {

                        pay.setStatus(PaymentStatus.APPROVED);
                        pay.setMessage("Payment Approved Automatically");

                    } else {

                        pay.setStatus(PaymentStatus.PENDING_APPROVAL);
                        pay.setMessage("Waiting for Checker Approval");
                    }

                    paymentRepo.save(pay);

                    f.setStatus(RetryStatus.COMPLETED);
                    f.setUpdatedAt(LocalDateTime.now());

                    failedRepo.save(f);

                    saveAudit(pay, "FRAUD RETRY SUCCESS");
                }

                
                else {

                    pay.setStatus(PaymentStatus.FAILED);
                    pay.setMessage(fraudResponse.getMessage());

                    paymentRepo.save(pay);

                    f.setStatus(RetryStatus.FAILED);
                    f.setUpdatedAt(LocalDateTime.now());

                    failedRepo.save(f);

                    saveAudit(pay, "FRAUD RETRY FAILED");
                }

            }
            catch (Exception ex) {

                f.setRetryCount(f.getRetryCount() + 1);

                f.setFailureType(ex.getClass().getSimpleName());

                f.setUpdatedAt(LocalDateTime.now());

                failedRepo.save(f);

                Payment pay =
                        paymentRepo.findById(f.getTransactionId())
                                .orElse(null);

                if (pay != null) {
                    saveAudit(pay, "FRAUD RETRY ATTEMPT");
                }

                System.out.println("Retry Failed : " + ex.getMessage());
            }

        }

    }

    private void saveAudit(Payment pay, String action) {

        AuditLog log = new AuditLog();

        log.setUsername("SYSTEM");
        log.setRole("SYSTEM");

        log.setAction(action);

        log.setPaymentId(pay.getId());

        log.setPaymentType(pay.getPaymentType());

        log.setAmount(pay.getAmount());

        log.setPaymentStatus(pay.getStatus().name());

        log.setActionTime(LocalDateTime.now());

        auditRepo.save(log);
    }

}