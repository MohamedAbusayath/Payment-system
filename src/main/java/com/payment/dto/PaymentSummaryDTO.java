package com.payment.dto;

import jakarta.validation.constraints.NegativeOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@NegativeOrZero
@Data
public class PaymentSummaryDTO {
	private long totalTransaction;
	private long successfulTransaction;
	private long failedtransaction;
}
