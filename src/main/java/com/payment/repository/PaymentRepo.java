package com.payment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.payment.entity.Payment;
import com.payment.enums.PaymentStatus;

@Repository
public interface PaymentRepo extends JpaRepository<Payment, Long>,JpaSpecificationExecutor<Payment>{

	int countByStatus(PaymentStatus approved);
	List<Payment>findByStatus(PaymentStatus status);
	List<Payment> findByCreatedBy(String username);
	List<Payment> findByPaymentTypeContainingIgnoreCase(String keyword);
}
