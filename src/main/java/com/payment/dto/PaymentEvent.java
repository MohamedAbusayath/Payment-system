package com.payment.dto;

import java.time.LocalDateTime;
import com.payment.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {

	private long paymentId;
	private String paymentType;
	private double amount;
	private PaymentStatus status;
	private String email;
	private String createdBy;
	private String message;
	private LocalDateTime paymentTime;
	
	
}
