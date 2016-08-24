package fr.sebnuss.sleuth.repro;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.sleuth.Span;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author snussbaumer
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CheckHeadersTest.ZuulApplication.class)
@WebIntegrationTest
public class CheckHeadersTest {

    private static Logger log = LoggerFactory.getLogger(CheckHeadersTest.class);

    @Test
    public void verifyTraceHeadersPresentOnlyOnce() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> result = restTemplate.exchange("http://localhost:8080/test", HttpMethod.GET, null,
                String.class);
        assertThat(result.getBody()).isEqualTo("OK");
        log.info("Headers : {}", result.getHeaders());
        assertThat(result.getHeaders().get(Span.TRACE_ID_NAME)).hasSize(1);
    }

    @EnableZuulProxy
    @SpringCloudApplication
    @RestController
    public static class ZuulApplication {

        private static Logger log = LoggerFactory.getLogger(ZuulApplication.class);

        @RequestMapping(method = RequestMethod.GET, path = "/real/test")
        public String test() {
            log.info("test is being called");
            return "OK";
        }

        public static void main(String... args) {
            SpringApplication.run(ZuulApplication.class, args);
        }

    }

}
