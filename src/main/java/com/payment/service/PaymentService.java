package com.payment.service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.dto.FraudRequest;
import com.payment.dto.FraudResponse;
import com.payment.dto.PaymentEvent;
import com.payment.dto.PaymentEventDTO;
import com.payment.dto.PaymentHistoryDTO;
import com.payment.dto.PaymentRequestDTO;
import com.payment.dto.PaymentResponseDTO;
import com.payment.dto.PaymentSummaryDTO;
import com.payment.dto.ReportRequestDTO;
import com.payment.entity.AuditLog;
import com.payment.entity.FailedTransaction;
import com.payment.entity.Payment;
import com.payment.enums.PaymentStatus;
import com.payment.enums.RetryStatus;
import com.payment.exception.InvalidPaymentException;
import com.payment.notification.PaymentEventProducer;
import com.payment.repository.AuditLogRepository;
import com.payment.repository.FailedTransactionRepository;
import com.payment.repository.PaymentRepo;

@Service
public class PaymentService {

	private final PaymentRepo repo;
	private final AuditLogRepository au_repo;
	private final RestTemplate rest;
	private final FailedTransactionRepository failedRepo;
	private final ObjectMapper objectMapper;
	private final PaymentEventProducer produce;

	@Autowired
	public PaymentService(PaymentRepo repo, AuditLogRepository au_repo, RestTemplate rest,
			FailedTransactionRepository failedRepo, ObjectMapper objectMapper, PaymentEventProducer produce) {
		this.repo = repo;
		this.au_repo = au_repo;
		this.rest = rest;
		this.failedRepo = failedRepo;
		this.objectMapper = objectMapper;
		this.produce = produce;
	}

	public PaymentResponseDTO savePay(PaymentRequestDTO req, String username)
			throws InvalidPaymentException, JsonProcessingException {

		if ("CARD".equals(req.getPaymentType())) {

			if (req.getCardNo() == null || req.getCardNo().isBlank()) {
				throw new InvalidPaymentException("Card Number Required");
			}

		}

		if ("BANK".equals(req.getPaymentType())) {

			if (req.getAccountNo() == null || req.getAccountNo().isBlank()) {
				throw new InvalidPaymentException("Account Number Required");
			}

			if (req.getBankName() == null || req.getBankName().isBlank()) {
				throw new InvalidPaymentException("Bank Name Required");
			}

		}

		Payment payment = new Payment();

		payment.setAmount(req.getAmount());
		payment.setPaymentType(req.getPaymentType());
		payment.setCreatedBy(username);
		payment.setPaymentTime(LocalDateTime.now());
		payment.setEmail(req.getEmail());
		payment.setStatus(PaymentStatus.FRAUD_PENDING);
		payment.setMessage("Fraud Verification Pending");

		payment = repo.save(payment);

		FraudRequest fraudRequest = new FraudRequest();

		fraudRequest.setCardNo(req.getCardNo());
		fraudRequest.setAmount(req.getAmount());

		String url = "http://localhost:8081/fraud/check";

		try {

			FraudResponse fraudResponse = rest.postForObject(url, fraudRequest, FraudResponse.class);

			if ("SAFE".equals(fraudResponse.getStatus())) {

				if (payment.getAmount() <= 1000) {

					payment.setStatus(PaymentStatus.APPROVED);
					payment.setMessage("Payment Approved Automatically");

				} else {

					payment.setStatus(PaymentStatus.PENDING_APPROVAL);
					payment.setMessage("Waiting for Checker Approval");

				}

			}

			else {

				payment.setStatus(PaymentStatus.FAILED);
				payment.setMessage(fraudResponse.getMessage());

			}

			repo.save(payment);

		}

		catch (Exception ex) {

			FailedTransaction failed = new FailedTransaction();

			failed.setTransactionId(payment.getId());

			failed.setRequest(objectMapper.writeValueAsString(fraudRequest));

			failed.setFailureType(ex.getClass().getSimpleName());

			failed.setRetryCount(0);

			failed.setMaxRetryCount(10);

			failed.setStatus(RetryStatus.PENDING);

			failed.setCreatedAt(LocalDateTime.now());

			failed.setUpdatedAt(LocalDateTime.now());

			failedRepo.save(failed);

		}

		AuditLog log = new AuditLog();

		log.setUsername(username);
		log.setRole("MAKER");
		log.setAction("CREATED PAYMENT");
		log.setPaymentId(payment.getId());
		log.setPaymentType(payment.getPaymentType());
		log.setAmount(payment.getAmount());
		log.setPaymentStatus(payment.getStatus().name());
		log.setActionTime(LocalDateTime.now());

		au_repo.save(log);

		PaymentResponseDTO res = new PaymentResponseDTO();

		res.setStatus(payment.getStatus());
		res.setMessage(payment.getMessage());
		res.setPaymentType(payment.getPaymentType());
		res.setAmount(payment.getAmount());
		res.setPaymentTime(payment.getPaymentTime());

		produce.publishPayEvent(createPaymentEvent(payment));

		return res;
	}

	// producer method......
	private PaymentEvent createPaymentEvent(Payment payment) {

		PaymentEvent event = new PaymentEvent();

		event.setPaymentId(payment.getId());
		event.setPaymentType(payment.getPaymentType());
		event.setAmount(payment.getAmount());
		event.setStatus(payment.getStatus());
		event.setEmail(payment.getEmail());
		event.setMessage(payment.getMessage());
		event.setPaymentTime(payment.getPaymentTime());

		return event;
	}

	public List<Payment> pendingStatus() {
		return repo.findByStatus(PaymentStatus.PENDING_APPROVAL);
	}

	public Payment approvePayment(Long id, String user) {
		Payment u = repo.findById(id).orElseThrow(() -> new UsernameNotFoundException("User not found"));
		u.setStatus(PaymentStatus.APPROVED);
		u.setMessage("Approved By Checker");
		Payment save = repo.save(u);
		AuditLog log = new AuditLog();

		log.setUsername(user);
		log.setRole("CHECKER");
		log.setAction("APPROVED PAYMENT");
		log.setPaymentId(save.getId());
		log.setPaymentType(save.getPaymentType());
		log.setAmount(save.getAmount());
		log.setPaymentStatus(save.getStatus().name());
		log.setActionTime(LocalDateTime.now());

		au_repo.save(log);
		produce.publishPayEvent(createPaymentEvent(save));// approve produce
		return save;
	}

	public Payment rejectPay(Long id, String user) {
		Payment u = repo.findById(id).orElseThrow(() -> new UsernameNotFoundException("User not found"));
		u.setStatus(PaymentStatus.REJECTED);
		u.setMessage("Rejected By Checker");
		Payment save = repo.save(u);
		AuditLog log = new AuditLog();
		log.setUsername(user);
		log.setRole("CHECKER");
		log.setAction("REJECTED PAYMENT");
		log.setPaymentId(save.getId());
		log.setPaymentType(save.getPaymentType());
		log.setAmount(save.getAmount());
		log.setPaymentStatus(save.getStatus().name());
		log.setActionTime(LocalDateTime.now());

		au_repo.save(log);
		produce.publishPayEvent(createPaymentEvent(save));
		return save;
	}

	public PaymentSummaryDTO paymentSum() {
		PaymentSummaryDTO sum = new PaymentSummaryDTO();
		long total = repo.count();
		int ap = repo.countByStatus(PaymentStatus.APPROVED);
		int re = repo.countByStatus(PaymentStatus.REJECTED);
		sum.setTotalTransaction(total);
		sum.setSuccessfulTransaction(ap);
		sum.setFailedtransaction(re);
		return sum;
	}

	public List<Payment> myPay(String name) {
		List<Payment> list = repo.findByCreatedBy(name);
		return list;
	}

	public List<Payment> search(String keyword) {
		return repo.findByPaymentTypeContainingIgnoreCase(keyword);
	}

	public Page<Payment> get(int page, int size, String keyword) {
		PageRequest p = PageRequest.of(page, size);
		Specification<Payment> sp = (r, q, cB) -> {
			if (keyword == null || keyword.isBlank())
				return cB.conjunction();
			return cB.or(cB.like(cB.lower(r.get("paymentType")), "%" + keyword.toLowerCase() + "%"),
					cB.like(cB.lower(r.get("createdBy")), "%" + keyword.toLowerCase() + "%"),
					cB.equal(r.get("id"), isNumber(keyword) ? Long.valueOf(keyword) : -1L),
					cB.equal(r.get("amount"), isNumber(keyword) ? Long.valueOf(keyword) : -1L));
		};
		return repo.findAll(sp, p);

	}

	private boolean isNumber(String keyword) {

		try {
			Double.parseDouble(keyword);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public List<AuditLog> history(Authentication auth) {
		String name = auth.getName();
		String role = auth.getAuthorities().iterator().next().getAuthority();

		if (role.equals("ROLE_ADMIN"))
			return au_repo.findAll();

		return au_repo.findByUsername(name);
	}

	public ByteArrayInputStream report(ReportRequestDTO req) throws IOException {
		LocalDateTime start = req.getStart().atStartOfDay();
		LocalDateTime end = req.getEnd().atTime(23, 59, 59);

		List<Payment> payments = repo.findByStatusAndPaymentTimeBetween(req.getStatus(), start, end);
		Workbook wrkBook = new XSSFWorkbook();
		Sheet sht = wrkBook.createSheet("Payments");
		Row header = sht.createRow(0);
		header.createCell(0).setCellValue("ID");
		header.createCell(1).setCellValue("Payment Type");
		header.createCell(2).setCellValue("Amount");
		header.createCell(3).setCellValue("Status");
		header.createCell(4).setCellValue("Created By");
		header.createCell(5).setCellValue("Payment Time");

		int rowNum = 1;

		for (Payment p : payments) {
			Row r = sht.createRow(rowNum++);
			r.createCell(0).setCellValue(p.getId());
			r.createCell(1).setCellValue(p.getPaymentType());
			r.createCell(2).setCellValue(p.getAmount());
			r.createCell(3).setCellValue(p.getStatus().toString());
			r.createCell(4).setCellValue(p.getCreatedBy());
			r.createCell(5).setCellValue(p.getPaymentTime().format(DateTimeFormatter.ISO_DATE));
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		wrkBook.write(out);
		wrkBook.close();

		return new ByteArrayInputStream(out.toByteArray());
	}

	public PaymentHistoryDTO getPaymentHistory(Long paymentId) {

		Payment payment = repo.findById(paymentId)
				.orElseThrow(() -> new UsernameNotFoundException("Payment Not Found"));

		List<AuditLog> logs = au_repo.findByPaymentIdOrderByActionTimeAsc(paymentId);

		PaymentHistoryDTO history = new PaymentHistoryDTO();

		history.setPaymentId(paymentId);

		if (payment.getStatus() == PaymentStatus.PENDING_APPROVAL) {
			history.setFinalStatus("IN PROGRESS");
		} else if (payment.getStatus() == PaymentStatus.APPROVED) {
			history.setFinalStatus("SUCCESS");
		} else {
			history.setFinalStatus("FAILED");
		}

		List<PaymentEventDTO> events = new ArrayList<>();

		for (AuditLog log : logs) {

			PaymentEventDTO dto = new PaymentEventDTO();

			dto.setAction(log.getAction());
			dto.setUsername(log.getUsername());
			dto.setRole(log.getRole());
			dto.setActionTime(log.getActionTime());

			events.add(dto);
		}

		history.setEvents(events);

		return history;
	}

}