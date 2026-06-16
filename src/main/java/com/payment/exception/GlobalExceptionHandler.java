package com.payment.exception;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.payment.dto.PaymentResponseDTO;
import com.payment.entity.Payment;
import com.payment.repository.PaymentRepo;

@RestControllerAdvice
public class GlobalExceptionHandler {
	PaymentRepo repo;
	@Autowired
	public GlobalExceptionHandler(PaymentRepo repo) {
		this.repo=repo;
	}
	
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String Ex(Exception e) {
		return e.getMessage();
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public PaymentResponseDTO handleValidation(
	        MethodArgumentNotValidException ex) {
	      
		   Payment payment = new Payment();
		   payment.setStatus("FAILED");
		   payment.setMessage( ex.getBindingResult()
		              .getFieldError()
		              .getDefaultMessage());
		   payment.setPaymentTime(LocalDateTime.now());
		   payment.setPaymentType("INVALID_REQUEST");

		  Payment save = repo.save(payment);
		  PaymentResponseDTO res=new PaymentResponseDTO();
	    	res.setStatus(save.getStatus());
	    	res.setMessage(save.getMessage());
	    	res.setPaymentType(save.getPaymentType());
	    	res.setPaymentTime(save.getPaymentTime());
	    	res.setAmount(save.getAmount());
	    return res;
	}
}
