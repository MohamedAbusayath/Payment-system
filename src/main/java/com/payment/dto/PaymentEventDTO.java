package com.payment.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PaymentEventDTO {

	   private String action;

	    private String username;

	    private String role;

	    private LocalDateTime actionTime;
}
