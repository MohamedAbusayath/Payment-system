package com.payment.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.payment.dto.PaymentEvent;

import lombok.RequiredArgsConstructor;

@Service
public class EmailService {

	private final JavaMailSender mailSender;

	public EmailService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	private static final Logger log= LoggerFactory.getLogger(EmailService.class);

	public void sendNotification(PaymentEvent event) {

		SimpleMailMessage msg = new SimpleMailMessage();
		msg.setTo(event.getEmail());
		msg.setSubject("Payment Notification");
		msg.setText(builedMsg(event));

		try {
		mailSender.send(msg);
		log.info("Email sent Successfully");
		}catch (Exception e) {
			log.info("Email not send:{}",e.getMessage());
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
