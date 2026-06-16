package com.payment.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PaymentResponseDTO {
	String status;
	String message;
	String paymentType;
	double amount;
	LocalDateTime paymentTime;
}
