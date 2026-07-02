package com.payment.dto;

import lombok.*;


@NoArgsConstructor
@AllArgsConstructor
public class FraudRequest {

	private String cardNo;
	private Double amount;

	public String getCardNo() {
		return cardNo;
	}

	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}
}
