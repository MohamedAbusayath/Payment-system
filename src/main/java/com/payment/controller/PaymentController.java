package com.payment.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.payment.dto.PaymentRequestDTO;
import com.payment.dto.PaymentResponseDTO;
import com.payment.dto.PaymentSummaryDTO;
import com.payment.entity.Payment;
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
	public  ResponseEntity<PaymentResponseDTO> pay(@Valid @RequestBody PaymentRequestDTO req,Authentication auth) throws InvalidPaymentException {
		return ResponseEntity.ok(ser.savePay(req, auth.getName()));
	}
	@GetMapping("/payment/pending")
	public ResponseEntity<List<Payment>> pendingStatus(){
		return ResponseEntity.ok(ser.pendingStatus());
	}
	
	@PutMapping("payment/approve/{id}")
	public ResponseEntity<Payment> approve(@PathVariable Long id){
		return ResponseEntity.ok(ser.approvePayment(id));
	}
	@PutMapping("payment/reject/{id}")
	public ResponseEntity<Payment> reject(@PathVariable Long id){
		return ResponseEntity.ok(ser.rejectPay(id));
	}
	@GetMapping("payment/my")
	public ResponseEntity<List<Payment>> myPayments(Authentication auth){
		String name = auth.getName();
		return ResponseEntity.ok(ser.myPay(name));
	}
	@GetMapping("payment/details")
	public ResponseEntity<PaymentSummaryDTO> details() {
		return ResponseEntity.ok(ser.paymentSum());
	}
	
}
