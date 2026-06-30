package com.payment.dto;

import java.time.LocalDate;

import com.payment.enums.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequestDTO {
	
	private PaymentStatus status;
	
	private LocalDate start;
	private LocalDate end;
}
