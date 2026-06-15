package com.payment.paymentClass;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class CreditCardPayment extends PaymentMethod {

	@NotNull(message = "CardNO is missing")
	@Pattern(regexp = "\\d{8}",message = "card Number must be 8 digit only")
    private String cardNo;
	@NotNull(message = "CardNO is missing")
    private String cardHolder;	

    public CreditCardPayment(double amount,
                             String cardNo,
                             String cardHolder) {

        super(amount);

        this.cardNo = cardNo;
        this.cardHolder=cardHolder;
    }

    @Override
    public String getPaymentType() {
        return "Credit Card";
    }

}
