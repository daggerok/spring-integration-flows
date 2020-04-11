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
import java.util.Map;

@SpringBootApplication
class SpringIntegrationFlowTypedWebfluxApp {
  public static void main(String[] args) {
    SpringApplication.run(SpringIntegrationFlowTypedWebfluxApp.class, args);
  }
}

@Log4j2
@Configuration
@EnableIntegration
class SpringIntegrationFlowTypedWebfluxAppConfig {

  @Bean
  IntegrationFlow typedWebfluxFlow() {
    return IntegrationFlows.from(WebFlux.inboundGateway("/webflux-typed-test")
                                        .requestMapping(spec -> spec.produces(MediaType.TEXT_EVENT_STREAM_VALUE))
                                        .requestPayloadType(Map.class))
                           .handle(Map.class, (payload, headers) -> {
                             log.info("handling request payload: {}", payload);
                             log.info("handling request headers: {}", headers);
                             return Flux.just(Map.of("res", "ololo"),
                                              Map.of("res", "trololo"),
                                              Map.of("res", "map"));
                           })
                           .get();
  }
}

@SpringBootTest(
    classes = SpringIntegrationFlowTypedWebfluxApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringIntegrationFlowTypedWebfluxAppTest {

  @LocalServerPort
  Integer port;

  @Test
  void test() {
    var webClient = WebClient.builder().build();
    var flux = webClient.post()
                        .uri(String.format("http://127.0.0.1:%d/webflux-typed-test", port))
                        .body(Mono.just(Map.of("ololo", "trololo")), Map.class)
                        .retrieve()
                        .bodyToFlux(Map.class);

    StepVerifier.create(flux)
                .expectNextMatches(map -> "ololo".equals(map.get("res")))
                .expectNextMatches(map -> "trololo".equals(map.get("res")))
                .expectNextMatches(map -> "map".equals(map.get("res")))
                .verifyComplete();
  }

  @Test
  void negative_test() {
    var webClient = WebClient.builder().build();
    var flux = webClient.post()
                        .uri(String.format("http://127.0.0.1:%d/webflux-typed-test", port))
                        .body(Mono.just(List.of("ololo", "trololo")), List.class)
                        .retrieve()
                        .bodyToFlux(String.class);

    StepVerifier.create(flux)
                .expectErrorMatches(e -> e.getLocalizedMessage().endsWith("/webflux-typed-test")
                    && e.getLocalizedMessage().startsWith("500 Internal Server Error from POST"))
                .verify();
  }
}
