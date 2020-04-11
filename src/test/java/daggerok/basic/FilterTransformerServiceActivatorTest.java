package daggerok.basic;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;

@Log4j2
@Configuration
class FilterTransformerServiceActivatorApp {

  @Bean
  IntegrationFlow testFlow() {
    return IntegrationFlows.from("input")
                           .filter("hello"::contains)
                           .transform(" world"::concat)
                           .handle(System.out::println)
                           .get();
  }
}

@SpringBootTest(classes = FilterTransformerServiceActivatorApp.class)
class FilterTransformerServiceActivatorTest {

  @Test
  void test() {
    //
  }
}
