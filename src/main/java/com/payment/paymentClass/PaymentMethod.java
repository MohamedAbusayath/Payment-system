package com.payment.paymentClass;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Positive;
import lombok.NoArgsConstructor;


@NoArgsConstructor
public abstract class PaymentMethod {

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

	@Positive(message = "Invalid amount")
    private double amount;
    private LocalDateTime paymentTime;

    public PaymentMethod(double amount) {
        this.amount = amount;
        this.paymentTime = LocalDateTime.now();
    }

    public abstract String getPaymentType();
    
}