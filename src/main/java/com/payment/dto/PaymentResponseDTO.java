package com.payment.dto;

import java.time.LocalDateTime;

import com.payment.enums.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PaymentResponseDTO {
	PaymentStatus status;
	String message;
	String paymentType;
	double amount;
	LocalDateTime paymentTime;
}
