package com.payment.notification;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.payment.dto.PaymentEvent;

import lombok.RequiredArgsConstructor;

@Service
public class PaymentEventConsumer {

	private final EmailService emailSer;

	public PaymentEventConsumer(EmailService emailSer) {
		this.emailSer = emailSer;
	}

	@KafkaListener(topics = "payment-events", groupId = "payment-group")
	public void consume(PaymentEvent event) {
		System.out.println("Received Event: "+event);
		emailSer.sendNotification(event);
	}
}
