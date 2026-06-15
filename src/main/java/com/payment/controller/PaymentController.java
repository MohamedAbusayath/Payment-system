package com.payment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.payment.paymentClass.BankTransationPayment;
import com.payment.paymentClass.CreditCardPayment;
import com.payment.paymentClass.PaymentResponse;
import com.payment.paymentClass.WalletPayment;
import com.payment.service.PaymentService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api")
public class PaymentController {
	
	PaymentService ser;
	@Autowired
	public PaymentController(PaymentService ser) {
		this.ser=ser;
	}
	
	@PostMapping("payment/card")
	public PaymentResponse card(@Valid @RequestBody CreditCardPayment pay){
		return ser.processPayment(pay);
	}
	
	@PostMapping("payment/wallet")
	public PaymentResponse wallet(@Valid @RequestBody WalletPayment pay){
		return ser.processPayment(pay);
	}
	
	@PostMapping("payment/bank")
	public PaymentResponse bank(@Valid @RequestBody BankTransationPayment pay){
		return ser.processPayment(pay);
	}
	
	@GetMapping("payment/details")
	public String details() {
		return ser.transactionDetails();
	}
}
