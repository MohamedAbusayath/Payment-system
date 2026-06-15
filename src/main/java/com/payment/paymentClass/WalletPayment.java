package com.payment.paymentClass;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;


public class WalletPayment extends PaymentMethod {


	@NotNull(message = "Missing Number and Number must be 6 digit")
	@Pattern(regexp = "\\d{6}",message = "Missing Number and Number must be 6 digit")
	String walletId;
	


	public WalletPayment(double amount,
			@NotNull(message = "Missing Number and Number must be 6 digit") @Pattern(regexp = "\\d{6}", message = "Missing Number and Number must be 6 digit") String walletId) {
		super(amount);
		this.walletId = walletId;
	}



	@Override
	public String getPaymentType() {
		return "Wallet";
	}
}
