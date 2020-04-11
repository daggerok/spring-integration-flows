package daggerok.webflux;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.webflux.dsl.WebFlux;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

@SpringBootApplication
class SpringIntegrationFlowWebfluxApp {
  public static void main(String[] args) {
    SpringApplication.run(SpringIntegrationFlowWebfluxApp.class, args);
  }
}

@Log4j2
@Configuration
@EnableIntegration
class SpringIntegrationFlowWebfluxAppConfig {

  @Bean
  IntegrationFlow sseWebfluxFlow() {
    return IntegrationFlows.from(WebFlux.inboundGateway("/webflux-sse-test")
                                        .requestMapping(spec -> spec.produces(MediaType.TEXT_EVENT_STREAM_VALUE)))
                           // .handle(Object.class, (payload, headers) -> {
                           //   log.info("handling request payload: {}", payload);
                           //   log.info("handling request headers: {}", headers);
                           //   return Flux.just("ololo", "trololo", "object");
                           // })
                           .handle(List.class, (payload, headers) -> {
                             log.info("handling request payload: {}", payload);
                             log.info("handling request headers: {}", headers);
                             return Flux.just("ololo", "trololo", "list");
                           })
                           .get();
  }
}

@SpringBootTest(
    classes = SpringIntegrationFlowWebfluxApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringIntegrationFlowWebfluxAppTest {

  @LocalServerPort
  Integer port;

  @Test
  void test() {
    var webClient = WebClient.builder().build();
    var flux = webClient.post()
                        .uri(String.format("http://127.0.0.1:%d/webflux-sse-test", port))
                        // .body(Mono.just("[\"hello\",\"world\"]"), String.class)
                        .body(Mono.just(List.of("ololo", "trololo")), List.class)
                        .retrieve()
                        .bodyToFlux(String.class);
    StepVerifier.create(flux)
                .expectNext("ololo")
                .expectNext("trololo")
                .expectNext("list")
                .verifyComplete();
  }
}
