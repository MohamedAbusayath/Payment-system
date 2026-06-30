package com.payment.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PaymentHistoryDTO {

   	   private Long paymentId;

	    private String finalStatus;

	    private List<PaymentEventDTO> events;
}
