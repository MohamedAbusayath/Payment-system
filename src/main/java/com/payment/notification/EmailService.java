package com.payment.notification;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.payment.dto.PaymentEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

	private final JavaMailSender mailSender;

	public void sendNotification(PaymentEvent event) {

		SimpleMailMessage msg = new SimpleMailMessage();
		msg.setTo(event.getEmail());
		msg.setSubject("Payment Notification");
		msg.setText(builedMsg(event));

		try {
		mailSender.send(msg);
		System.out.println("Email Sent Successfully");
		}catch (Exception e) {
			System.out.println("Email not sent: "+e.getMessage());
		}
	}

	private String builedMsg(PaymentEvent event) {

		return """
				Dear Customer,

				Your payment Status:

				            Payment ID : %d
				            Payment Type : %s
				            Amount : %.2f
				            Status : %s
				            Message : %s
				            Time : %s

				Thank You.

				 """.formatted(event.getPaymentId(), event.getPaymentType(), event.getAmount(), event.getStatus(),
				event.getMessage(), event.getPaymentTime());
	}

}
