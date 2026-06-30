package com.payment.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PaymentEventDTO {

	   private String action;

	    private String username;

	    private String role;

	    private LocalDateTime actionTime;
}
