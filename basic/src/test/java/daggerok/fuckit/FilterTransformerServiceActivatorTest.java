package daggerok.fuckit;

import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.MessageChannel;

@Log4j2
@Configuration
@EnableIntegration
class FilterTransformerServiceActivatorApp {

  @Bean
  MessageChannel input() {
    return MessageChannels.direct().get();
  }

  @Bean
  IntegrationFlow testFlow() {
    return IntegrationFlows.from("input").log("== 1 ==")
                           .filter("world"::equals).log("== 2 ==")
                           .transform("hello "::concat).log("== 3 ==")
                           .handle(System.out::println)
                           .get();
  }
}

@AllArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest(classes = FilterTransformerServiceActivatorApp.class)
class FilterTransformerServiceActivatorTest {

  MessageChannel input;

  @Test
  void test() {
    Try.run(() -> Thread.sleep(1234));
  }
}
