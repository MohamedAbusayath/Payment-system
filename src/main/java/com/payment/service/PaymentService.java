package com.payment.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import com.payment.dto.PaymentRequestDTO;
import com.payment.dto.PaymentResponseDTO;
import com.payment.dto.PaymentSummaryDTO;
import com.payment.entity.Payment;
import com.payment.enums.PaymentStatus;
import com.payment.exception.InvalidPaymentException;
import com.payment.repository.PaymentRepo;

@Service
public class PaymentService {

	private final PaymentRepo repo;

	@Autowired
	public PaymentService(PaymentRepo repo) {
		this.repo = repo;
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

	public Payment approvePayment(Long id) {
		Payment u = repo.findById(id).orElseThrow(() -> new UsernameNotFoundException("User not found"));
		u.setStatus(PaymentStatus.APPROVED);
		u.setMessage("Approved By Checker");
		return repo.save(u);
	}

	public Payment rejectPay(Long id) {
		Payment u = repo.findById(id).orElseThrow(() -> new UsernameNotFoundException("User not found"));
		u.setStatus(PaymentStatus.REJECTED);
		u.setMessage("Rejected By Checker");
		return repo.save(u);
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

	public Page<Payment> getPay(int page, int size) {
		PageRequest p = PageRequest.of(page, size, Sort.by("amount"));
		return repo.findAll(p);
	}

	

	public List<Payment> search(String keyword) {
		return repo.findByPaymentTypeContainingIgnoreCase(keyword);
	}
}