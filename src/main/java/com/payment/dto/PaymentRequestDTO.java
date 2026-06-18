package com.payment.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PaymentRequestDTO {
	
	@Positive(message = "Amount must be greater than zero")
	double amount;
	@NotNull(message = "Payment Type is Missing")
	@NotEmpty(message = "Payment Type is Missing")
	String paymentType;
	String cardNo;
	String cardHolder;
	String accountNo;
	String bankName;
	String walletId;
}
