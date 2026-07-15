package Cucumber.config;

import com.payment.PaymentSystemApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

@CucumberContextConfiguration
@SpringBootTest(
        classes = PaymentSystemApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class CucumberSpringConfiguration {
    @MockitoBean
    private RestTemplate restTemplate;
}
