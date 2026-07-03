package com.payment.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
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

	@Value("${fraud.service.url}")
	private String url;

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

		validate(req);

		Payment payment=createPay(req,username);

		payment = repo.save(payment);


		FraudRequest fraudRequest = new FraudRequest();
		fraudRequest.setCardNo(req.getCardNo());
		fraudRequest.setAmount(req.getAmount());

		try {

			FraudResponse fraudResponse = rest.postForObject(url, fraudRequest, FraudResponse.class);
			if(fraudResponse==null){
				throw new NullPointerException("Fraud Response is null");
			}
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

		saveAudit(payment,username,"MAKER","PAYMENT CREATED");

		produce.publishPayEvent(createPaymentEvent(payment));

		return buildResponse(payment);
	}



	public List<Payment> pendingStatus() {
		return repo.findByStatus(PaymentStatus.PENDING_APPROVAL);
	}

	public Payment approvePayment(Long id, String user) {
		Payment u=getPayment(id);
		u.setStatus(PaymentStatus.APPROVED);
		u.setMessage("Approved By Checker");
		Payment save = repo.save(u);

		saveAudit(save,user,"CHECKER","APPROVED PAYMENT");

		produce.publishPayEvent(createPaymentEvent(save));// approve produce
		return save;
	}

	public Payment rejectPay(Long id, String user) {
		Payment u=getPayment(id);
		u.setStatus(PaymentStatus.REJECTED);
		u.setMessage("Rejected By Checker");
		Payment save = repo.save(u);

		saveAudit(save,user,"CHECKER","REJECTED PAYMENT");

		produce.publishPayEvent(createPaymentEvent(save));
		return save;
	}


	public List<Payment> myPay(String name) {
		return repo.findByCreatedBy(name);
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


	public PaymentHistoryDTO getPaymentHistory(Long paymentId) {

		Payment payment=getPayment(paymentId);
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

	//GET PAYMENT ID
	private Payment getPayment(Long id){
		return repo.findById(id).orElseThrow(()-> new UsernameNotFoundException("Not Found"));
	}

	//AUDIT
	private void saveAudit(Payment payment,String username,String role,String action){
		AuditLog log = new AuditLog();
		log.setUsername(username);
		log.setRole(role);
		log.setAction(action);
		log.setPaymentId(payment.getId());
		log.setPaymentType(payment.getPaymentType());
		log.setAmount(payment.getAmount());
		log.setPaymentStatus(payment.getStatus().name());
		log.setActionTime(LocalDateTime.now());
		au_repo.save(log);
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

	//validate
	private void validate(PaymentRequestDTO req) throws InvalidPaymentException {
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

	}
	//request to set payment
	private Payment createPay(PaymentRequestDTO req,String user){

		Payment payment = new Payment();
		payment.setAmount(req.getAmount());
		payment.setPaymentType(req.getPaymentType());
		payment.setCreatedBy(user);
		payment.setPaymentTime(LocalDateTime.now());
		payment.setEmail(req.getEmail());
		payment.setStatus(PaymentStatus.FRAUD_PENDING);
		payment.setMessage("Fraud Verification Pending");
		return payment;
	}

	//response
	private PaymentResponseDTO buildResponse(Payment payment) {

		PaymentResponseDTO response = new PaymentResponseDTO();
		response.setStatus(payment.getStatus());
		response.setMessage(payment.getMessage());
		response.setPaymentType(payment.getPaymentType());
		response.setAmount(payment.getAmount());
		response.setPaymentTime(payment.getPaymentTime());
		return response;
	}
}