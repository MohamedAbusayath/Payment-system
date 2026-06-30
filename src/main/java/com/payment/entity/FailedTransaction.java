package com.payment.entity;

import java.time.LocalDateTime;

import com.payment.enums.RetryStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FailedTransaction {

	@Id
	@GeneratedValue(strategy =GenerationType.IDENTITY )
	private Long id;
	private Long transactionId;
	@Lob
	@Column(nullable = false)
	private String request;
	private String failureType;
	private int retryCount;
	private int maxRetryCount;
	private RetryStatus status;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

}
