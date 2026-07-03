package com.payment.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.payment.dto.PaymentEvent;


@Service
public class PaymentEventConsumer {

	private final EmailService emailSer;

	public PaymentEventConsumer(EmailService emailSer) {
		this.emailSer = emailSer;
	}

	private static final Logger log= LoggerFactory.getLogger(PaymentEventConsumer.class);

	@KafkaListener(topics = "payment-events", groupId = "payment-group")
	public void consume(PaymentEvent event) {
		log.info("Received Event:{}",event);
		emailSer.sendNotification(event);
	}
}
