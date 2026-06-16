package com.payment.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

import com.payment.dto.PaymentRequestDTO;
import com.payment.dto.PaymentResponseDTO;
import com.payment.dto.PaymentSummaryDTO;
import com.payment.entity.Payment;
import com.payment.exception.InvalidPaymentException;
import com.payment.repository.PaymentRepo;

@Service
public class PaymentService {
	
	PaymentRepo repo;
	@Autowired
	public PaymentService(PaymentRepo repo){
		this.repo=repo;
	}
    
    public PaymentResponseDTO savePay(PaymentRequestDTO req) throws InvalidPaymentException {
    	Payment pay=new Payment();
    	pay.setAmount(req.getAmount());
    	pay.setPaymentType(req.getPaymentType());
    	pay.setStatus("SUCCESS");
    	pay.setMessage("Payment Saved Successfully");
    	pay.setPaymentTime(LocalDateTime.now());
    	Payment save = repo.save(pay);
    	PaymentResponseDTO res=new PaymentResponseDTO();
    	res.setStatus(save.getStatus());
    	res.setMessage(save.getMessage());
    	res.setPaymentType(save.getPaymentType());
    	res.setPaymentTime(save.getPaymentTime());
    	res.setAmount(save.getAmount());
    	if(req.getPaymentType().equals("CARD")) {
    		if(req.getCardNo().isEmpty()) throw new InvalidPaymentException("card No is missing");
    		if(req.getCardHolder().isEmpty()) throw new InvalidPaymentException("card holder name is missing.");
    	}
    	if (req.getPaymentType().equals("B")) {
			if(req.getAccountNo().isBlank()) throw new InvalidPaymentException("Account no is missing");
			if(req.getBankName().isBlank()) throw new InvalidPaymentException("bank name is missing");
		}
    	return res;
    }
    
    public PaymentSummaryDTO paymentSum() {
    	PaymentSummaryDTO sum=new PaymentSummaryDTO();
    	long total = repo.count();
    	long success = repo.countByStatus("SUCCESS");
    	long failed = repo.countByStatus("FAILED");
    	sum.setTotalTransaction(total);
    	sum.setSuccessfulTransaction(success);
    	sum.setFailedtransaction(failed);
    	return sum;
    }
   
}