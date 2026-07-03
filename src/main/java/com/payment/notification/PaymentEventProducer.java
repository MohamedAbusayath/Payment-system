package com.payment.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.payment.dto.PaymentEvent;

@Service
public class PaymentEventProducer {

	private final KafkaTemplate<String, PaymentEvent> kafkaTem;

	public PaymentEventProducer(KafkaTemplate<String, PaymentEvent> kafkaTem) {
		this.kafkaTem = kafkaTem;
	}

	private static final Logger log= LoggerFactory.getLogger(PaymentEventProducer.class);

	private static final String TOPIC="payment-events";
	
	public void publishPayEvent(PaymentEvent event) {
		kafkaTem.send(TOPIC,event);
		log.info("payment event published:{}",event);
	}
}
