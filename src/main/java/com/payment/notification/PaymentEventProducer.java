package com.payment.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.payment.dto.PaymentEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentEventProducer {

	private final KafkaTemplate<String, PaymentEvent> kafkaTem;

	private static String TOPIC="payment-events";
	
	public void publishPayEvent(PaymentEvent event) {
		kafkaTem.send(TOPIC,event);
		System.out.println("payment event published:"+event);
	}
}
