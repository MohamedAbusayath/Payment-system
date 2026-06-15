package com.payment.service;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import com.payment.paymentClass.PaymentMethod;
import com.payment.paymentClass.PaymentResponse;

@Service
public class PaymentService {

    int successTransaction = 0;
    int failedTransaction = 0;
    int totalTransaction = 0;

    public PaymentResponse processPayment(PaymentMethod pay) {

        PaymentResponse res = new PaymentResponse();

        res.setAmount(pay.getAmount());
        res.setPaymentType(pay.getPaymentType());
        res.setStatus("SUCCESS");
        res.setMessage("Payment Processed Successfully");
        res.setPaymentTime(LocalDateTime.now());

        successTransaction++;
        totalTransaction++;

        return res;
    }
    
    public void failedTransaction() {
        failedTransaction++;
        totalTransaction++;
    }
    
    public String transactionDetails() {
    	return "successful Transaction:"+successTransaction+"\n"+"failed Transaction:"+failedTransaction+"\n"
    			+"Total Transaction:"+totalTransaction;
    }
}