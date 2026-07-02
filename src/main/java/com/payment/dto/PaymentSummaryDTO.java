package com.payment.dto;

import jakarta.validation.constraints.NegativeOrZero;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@NegativeOrZero

public class PaymentSummaryDTO {
	private long totalTransaction;
	private long successfulTransaction;
	private long failedtransaction;

	public long getTotalTransaction() {
		return totalTransaction;
	}

	public void setTotalTransaction(long totalTransaction) {
		this.totalTransaction = totalTransaction;
	}

	public long getSuccessfulTransaction() {
		return successfulTransaction;
	}

	public void setSuccessfulTransaction(long successfulTransaction) {
		this.successfulTransaction = successfulTransaction;
	}

	public long getFailedtransaction() {
		return failedtransaction;
	}

	public void setFailedtransaction(long failedtransaction) {
		this.failedtransaction = failedtransaction;
	}
}
