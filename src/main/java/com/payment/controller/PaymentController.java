package com.payment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.payment.dto.PaymentRequestDTO;
import com.payment.dto.PaymentResponseDTO;
import com.payment.dto.PaymentSummaryDTO;
import com.payment.exception.InvalidPaymentException;
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
	@PostMapping("/payment")
	public  ResponseEntity<PaymentResponseDTO> pay(@Valid @RequestBody PaymentRequestDTO req) throws InvalidPaymentException {
		return ResponseEntity.ok(ser.savePay(req));
	}
	@GetMapping("payment/details")
	public ResponseEntity<PaymentSummaryDTO> details() {
		return ResponseEntity.ok(ser.paymentSum());
	}
}
