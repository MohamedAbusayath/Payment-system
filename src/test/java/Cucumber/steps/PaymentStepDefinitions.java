package Cucumber.steps;

import com.payment.dto.FraudRequest;
import com.payment.dto.FraudResponse;
import com.payment.dto.PaymentRequestDTO;
import com.payment.dto.PaymentResponseDTO;
import com.payment.enums.PaymentStatus;
import com.payment.service.PaymentService;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class PaymentStepDefinitions {

    private double amount;

    private PaymentResponseDTO response;

    private final RestTemplate restTemplate;

    private final PaymentService paymentService;


    public PaymentStepDefinitions(
            PaymentService paymentService,
            RestTemplate restTemplate) {

        this.paymentService = paymentService;
        this.restTemplate = restTemplate;
    }


    @Given("a payment amount of {int}")
    public void a_payment_amount_of(Integer amount) {

        this.amount = amount;
    }


    @Given("the fraud service returns {string}")
    public void the_fraud_service_returns(String status) {

        FraudResponse fraudResponse =
                new FraudResponse();

        fraudResponse.setStatus(status);

        fraudResponse.setMessage(
                "SAFE".equals(status)
                        ? "Payment is Safe"
                        : "Fraud Detected"
        );


        when(
                restTemplate.exchange(
                        anyString(),
                        eq(org.springframework.http.HttpMethod.POST),
                        any(org.springframework.http.HttpEntity.class),
                        eq(FraudResponse.class)
                )
        ).thenReturn(
                ResponseEntity.ok(fraudResponse)
        );
    }


    @When("the payment is processed")
    public void the_payment_is_processed()
            throws Exception {

        PaymentRequestDTO request =
                new PaymentRequestDTO();

        request.setAmount(amount);

        request.setPaymentType("CARD");

        request.setCardNo(
                "1234567890123456"
        );

        request.setEmail(
                "test@gmail.com"
        );


        response = paymentService.savePay(
                request,
                "cucumber-test"
        );
    }


    @Then("the payment status should be {string}")
    public void the_payment_status_should_be(
            String expectedStatus) {

        assertEquals(
                PaymentStatus.valueOf(expectedStatus),
                response.getStatus()
        );
    }
}