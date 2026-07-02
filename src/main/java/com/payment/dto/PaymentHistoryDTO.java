package com.payment.dto;

import java.util.List;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor

public class PaymentHistoryDTO {

   	   private Long paymentId;

	    private String finalStatus;

	    private List<PaymentEventDTO> events;

	public Long getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(Long paymentId) {
		this.paymentId = paymentId;
	}

	public String getFinalStatus() {
		return finalStatus;
	}

	public void setFinalStatus(String finalStatus) {
		this.finalStatus = finalStatus;
	}

	public List<PaymentEventDTO> getEvents() {
		return events;
	}

	public void setEvents(List<PaymentEventDTO> events) {
		this.events = events;
	}
}
