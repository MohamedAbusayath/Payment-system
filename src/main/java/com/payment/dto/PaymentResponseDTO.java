package com.payment.dto;

import java.time.LocalDateTime;

import com.payment.enums.PaymentStatus;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor

public class PaymentResponseDTO {
	PaymentStatus status;
	String message;
	String paymentType;
	double amount;
	LocalDateTime paymentTime;

	public PaymentStatus getStatus() {
		return status;
	}

	public void setStatus(PaymentStatus status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public LocalDateTime getPaymentTime() {
		return paymentTime;
	}

	public void setPaymentTime(LocalDateTime paymentTime) {
		this.paymentTime = paymentTime;
	}
}
