package com.payment.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    private final FailedTransactionRepository failedRepo;

    private final PaymentRepo paymentRepo;

    private final AuditLogRepository auditRepo;

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    public RetrySchedulerService(ObjectMapper objectMapper,RestTemplate restTemplate,
                                 AuditLogRepository auditRepo,PaymentRepo paymentRepo,
                                 FailedTransactionRepository failedRepo) {
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
        this.auditRepo = auditRepo;
        this.paymentRepo = paymentRepo;
        this.failedRepo = failedRepo;
    }

    private static final Logger log =
            LoggerFactory.getLogger(RetrySchedulerService.class);

    @Value("${fraud.service.url}")
    private  String FRAUD_URL ;

    @Scheduled(fixedRate = 30000)
    public void retryFailedTransactions() {


        List<FailedTransaction> transactions =
                failedRepo.findByStatus(RetryStatus.PENDING);
        if (transactions.isEmpty()) {
            return;
        }
        System.out.println("Retrying " + transactions.size() + " failed transaction(s)");

        log.info("Retrying {} failed transaction(s)",transactions.size());

        for (FailedTransaction f : transactions) {

            try {

                
                if (f.getRetryCount() >= f.getMaxRetryCount()) {

                    Payment pay = getPayId(f.getTransactionId());

                    updatePaymentStatus(pay,PaymentStatus.FAILED,"Maximum Retry Reached");

                    updateRetryStatus(f,RetryStatus.FAILED);

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
              
                Payment pay =getPayId(f.getTransactionId());
                if(fraudResponse==null) throw new UsernameNotFoundException("FraudResponse Null");
                if ("SAFE".equals(fraudResponse.getStatus())) {

                    if (pay.getAmount() <= 1000) {
                        updatePaymentStatus(pay,PaymentStatus.APPROVED,"Payment Approved Automatically");
                    } else {

                        updatePaymentStatus(pay,PaymentStatus.PENDING_APPROVAL,"Waiting for Checker Approval");
                    }


                   updateRetryStatus(f,RetryStatus.COMPLETED);

                    saveAudit(pay, "FRAUD RETRY SUCCESS");
                }

                
                else {

                    updatePaymentStatus(pay,PaymentStatus.FAILED,fraudResponse.getMessage());

                    updateRetryStatus(f,RetryStatus.FAILED);

                    saveAudit(pay, "FRAUD RETRY FAILED");
                }

            }
            catch (Exception ex) {

                f.setRetryCount(f.getRetryCount() + 1);

                f.setFailureType(ex.getClass().getSimpleName());

                f.setUpdatedAt(LocalDateTime.now());

                failedRepo.save(f);

                Payment pay =getPayId(f.getTransactionId());

                if (pay != null) {
                    saveAudit(pay, "FRAUD RETRY ATTEMPT");
                }

                log.error("Retry Filed {}:",ex.getMessage());
            }

        }

    }

    //audit save
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


    //payment status
    private void updatePaymentStatus(Payment pay,PaymentStatus status,String msg){
        pay.setStatus(status);
        pay.setMessage(msg);
        paymentRepo.save(pay);
    }

    //get payment using id  if it not, throw the exception
    private Payment getPayId(Long id){
       return paymentRepo.findById(id).orElseThrow(()->new UsernameNotFoundException("Payment Not found"));
    }

    //Retry
    private void updateRetryStatus(FailedTransaction fail,
                                   RetryStatus status) {

        fail.setStatus(status);
        fail.setUpdatedAt(LocalDateTime.now());
        failedRepo.save(fail);
    }
}