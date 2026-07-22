package com.payment.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import com.payment.service.ReportService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.payment.dto.PaymentHistoryDTO;
import com.payment.dto.PaymentRequestDTO;
import com.payment.dto.PaymentResponseDTO;
import com.payment.dto.ReportRequestDTO;
import com.payment.entity.AuditLog;
import com.payment.entity.Payment;
import com.payment.exception.InvalidPaymentException;
import com.payment.service.PaymentService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@Tag(name = "Payment Management",
description = "APIs for creating, approving, rejecting, and viewing payments")
@SecurityRequirement(
		name = "Bearer Authentication"
)
public class PaymentController {

	PaymentService ser;
	ReportService reportSer;

	@Autowired
	public PaymentController(PaymentService ser,ReportService reportSer) {
		this.ser = ser;
		this.reportSer=reportSer;
	}

	@PostMapping("/payment")
	public ResponseEntity<PaymentResponseDTO> pay(@Valid @RequestBody PaymentRequestDTO req, Authentication auth)
			throws InvalidPaymentException, JsonProcessingException {
		return ResponseEntity.ok(ser.savePay(req, auth.getName()));
	}

	@GetMapping("/payment/pending")
	public ResponseEntity<List<Payment>> pendingStatus() {
		return ResponseEntity.ok(ser.pendingStatus());
	}

	@PutMapping("payment/approve/{id}")
	public ResponseEntity<Payment> approve(@PathVariable Long id, Authentication auth) {
		String name = auth.getName();
		return ResponseEntity.ok(ser.approvePayment(id, name));
	}

	@PutMapping("payment/reject/{id}")
	public ResponseEntity<Payment> reject(@PathVariable Long id, Authentication auth) {
		return ResponseEntity.ok(ser.rejectPay(id, auth.getName()));
	}

	@GetMapping("payment/my")
	public ResponseEntity<List<Payment>> myPayments(Authentication auth) {
		String name = auth.getName();
		return ResponseEntity.ok(ser.myPay(name));
	}


	@GetMapping("payment/get")
	public ResponseEntity<Page<Payment>> get(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int size, @RequestParam(required = false) String keyword) {
		return ResponseEntity.ok(ser.get(page, size, keyword));
	}

	@GetMapping("payment/history")
	public ResponseEntity<List<AuditLog>> history(Authentication auth) {
		return ResponseEntity.ok(ser.history(auth));
	}

	@PostMapping("payment/report")
	public ResponseEntity<InputStreamResource> generateReport(@RequestBody ReportRequestDTO request)
			throws IOException {

		ByteArrayInputStream in = reportSer.report(request);

		HttpHeaders headers = new HttpHeaders();

		headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payments-report.xlsx");

		return ResponseEntity.ok().headers(headers)
				.contentType(
						MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(new InputStreamResource(in));
	}
	
	@PostMapping("payment/his/{id}")
	public ResponseEntity<PaymentHistoryDTO> getHis(@RequestParam Long id){
		return ResponseEntity.ok(ser.getPaymentHistory(id));
		
	}
}
