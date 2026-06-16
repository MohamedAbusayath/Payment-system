package com.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.payment.entity.Payment;

@Repository
public interface PaymentRepo extends JpaRepository<Payment, Long>{

	int countByStatus(String str);
}
