package com.payment.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import com.payment.dto.PaymentRequestDTO;
import com.payment.dto.PaymentResponseDTO;
import com.payment.dto.PaymentSummaryDTO;
import com.payment.entity.AuditLog;
import com.payment.entity.Payment;
import com.payment.enums.PaymentStatus;
import com.payment.exception.InvalidPaymentException;
import com.payment.repository.AuditLogRepository;
import com.payment.repository.PaymentRepo;

@Service
public class PaymentService {

	private final PaymentRepo repo;
	private final AuditLogRepository au_repo;

	@Autowired
	public PaymentService(PaymentRepo repo, AuditLogRepository au_repo) {
		this.repo = repo;
		this.au_repo = au_repo;
	}

	public PaymentResponseDTO savePay(PaymentRequestDTO req, String username) throws InvalidPaymentException {
		if (req.getPaymentType().equals("CARD")) {
			if (req.getCardNo().isEmpty())
				throw new InvalidPaymentException("card No is missing");
			if (req.getCardHolder().isEmpty())
				throw new InvalidPaymentException("card holder name is missing.");
		}
		if (req.getPaymentType().equals("BANK")) {
			if (req.getAccountNo().isBlank())
				throw new InvalidPaymentException("Account no is missing");
			if (req.getBankName().isBlank())
				throw new InvalidPaymentException("bank name is missing");
		}
		Payment pay = new Payment();
		pay.setAmount(req.getAmount());
		pay.setPaymentType(req.getPaymentType());
		pay.setPaymentTime(LocalDateTime.now());
		pay.setCreatedBy(username);
		if (pay.getAmount() <= 1000) {
			pay.setStatus(PaymentStatus.APPROVED);
			pay.setMessage("Payment Approved Automatically");
		} else {
			pay.setStatus(PaymentStatus.PENDING_APPROVAL);
			pay.setMessage("Waiting for Checker Approval");
		}

		Payment save = repo.save(pay);
		AuditLog log=new AuditLog();
		log.setUsername(username);
		log.setRole("MAKER");
		log.setAction("CREATED PAYMENT");
		log.setPaymentId(save.getId());
		log.setPaymentType(save.getPaymentType());
		log.setAmount(save.getAmount());
		log.setPaymentStatus(
		        save.getStatus().name());
		log.setActionTime(
		        LocalDateTime.now());

		au_repo.save(log);
		
		PaymentResponseDTO res = new PaymentResponseDTO();
		res.setStatus(save.getStatus());
		res.setMessage(save.getMessage());
		res.setPaymentType(save.getPaymentType());
		res.setPaymentTime(save.getPaymentTime());
		res.setAmount(save.getAmount());

		return res;
	}

	public List<Payment> pendingStatus() {
		return repo.findByStatus(PaymentStatus.PENDING_APPROVAL);
	}

	public Payment approvePayment(Long id,String user) {
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
		log.setPaymentStatus(
		        save.getStatus().name());
		log.setActionTime(
		        LocalDateTime.now());

		au_repo.save(log);
		return save;
	}

	public Payment rejectPay(Long id,String user) {
		Payment u = repo.findById(id).orElseThrow(() -> new UsernameNotFoundException("User not found"));
		u.setStatus(PaymentStatus.REJECTED);
		u.setMessage("Rejected By Checker");
		Payment save = repo.save(u);
		AuditLog log=new AuditLog();
		log.setUsername(user);
		log.setRole("CHECKER");
		log.setAction("REJECTED PAYMENT");
		log.setPaymentId(save.getId());
		log.setPaymentType(save.getPaymentType());
		log.setAmount(save.getAmount());
		log.setPaymentStatus(
		        save.getStatus().name());
		log.setActionTime(
		        LocalDateTime.now());

		au_repo.save(log);
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

	public String delete(long id) {
		repo.deleteById(id);
		return "Payment Deleted";
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
		
		if(role.equals("ROLE_ADMIN")) return au_repo.findAll();
		
		return au_repo.findByUsername(name);
	}

	
	
	
	
}