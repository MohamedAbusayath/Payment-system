package com.payment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.payment.entity.FailedTransaction;
import com.payment.enums.RetryStatus;

@Repository
public interface FailedTransactionRepository extends JpaRepository<FailedTransaction, Long> {

	List<FailedTransaction> findByStatus(RetryStatus pending);


}
