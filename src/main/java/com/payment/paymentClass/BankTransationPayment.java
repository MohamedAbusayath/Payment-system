package com.payment.paymentClass;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;


public class BankTransationPayment extends PaymentMethod{
	
	
	
	@NotNull(message = "Missing Number and Number must be 8 digit")
	@Pattern(regexp = "\\d{8}",message = "Missing Number and Number must be 8 digit")
	String accNo;
	@NotNull(message = "Bank Name missing")
	@NotEmpty(message = "Bank Name Missing")
	String bankName;
	
	public BankTransationPayment(double amount,
			@NotNull(message = "Missing Number and Number must be 8 digit") @Pattern(regexp = "\\d{8}", message = "Missing Number and Number must be 8 digit") String accNo,
			@NotNull(message = "Bank Name missing") @NotEmpty(message = "Bank Name Missing") String bankName) {
		super(amount);
		this.accNo = accNo;
		this.bankName = bankName;
	}

	@Override
	public String getPaymentType() {
		return "Bank";
	}
	
	
	
}
