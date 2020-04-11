package daggerok.webflux;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.http.inbound.RequestMapping;
import org.springframework.integration.webflux.inbound.WebFluxInboundEndpoint;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootApplication
class SpringIntegrationWebfluxApp {
  public static void main(String[] args) {
    SpringApplication.run(SpringIntegrationWebfluxApp.class, args);
  }
}

@Log4j2
@Configuration
@EnableIntegration
class SpringIntegrationWebfluxAppConfig {

  @Bean
  WebFluxInboundEndpoint simpleInboundEndpoint() {
    var endpoint = new WebFluxInboundEndpoint();
    var requestMapping = new RequestMapping();
    requestMapping.setPathPatterns("/webflux-test");
    endpoint.setRequestChannelName("serviceChannel");
    endpoint.setRequestMapping(requestMapping);
    return endpoint;
  }

  @Bean
  MessageChannel serviceChannel() {
    return MessageChannels.flux().get();
  }

  @ServiceActivator(inputChannel = "serviceChannel")
  Mono<Map<String, String>> service() {
    log.info("responding via service activator");
    return Mono.just(Collections.singletonMap("result", "It works!"));
  }
}

@SpringBootTest(
    classes = SpringIntegrationWebfluxApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringIntegrationWebfluxAppTest {

  @Autowired
  ApplicationContext applicationContext;

  @LocalServerPort
  Integer port;

  @Test
  void test() {
    WebTestClient.bindToApplicationContext(applicationContext)
                 .build()
                 .get()
                 .uri(String.format("http://127.0.0.1:%d/webflux-test", port))
                 .exchange()
                 .expectStatus().isOk()
                 .expectBody(new ParameterizedTypeReference<Map<String, String>>() {})
                 .consumeWith(result -> {
                   var body = result.getResponseBody();
                   assertThat(body).isNotNull()
                                   .hasSize(1);
                   assertThat(body.get("result")).isNotNull()
                                                 .containsIgnoringCase("it works");
                 });
  }
}
