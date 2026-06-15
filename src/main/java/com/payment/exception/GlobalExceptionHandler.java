package com.payment.exception;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.payment.paymentClass.ErrorResponse;
import com.payment.service.PaymentService;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@Autowired
	private PaymentService ser;
	
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String Ex(Exception e) {
		return e.getMessage();
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleValidation(
	        MethodArgumentNotValidException ex) {
		
	    ErrorResponse response = new ErrorResponse();

	    response.setStatus("FAILED");
	    response.setMessage(
	            ex.getBindingResult()
	              .getFieldError()
	              .getDefaultMessage());

	    response.setTimestamp(LocalDateTime.now());
	    ser.failedTransaction();
	    return response;
	}
}
