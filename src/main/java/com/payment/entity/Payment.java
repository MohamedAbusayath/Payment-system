package com.payment.entity;

import java.time.LocalDateTime;

import com.payment.enums.PaymentStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name="payments")
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	private String paymentType;
	private double amount;
	@Enumerated(EnumType.STRING)
	private PaymentStatus status;
	private String email;
	private String createdBy;
	private String message;
	private LocalDateTime paymentTime;
	
}
