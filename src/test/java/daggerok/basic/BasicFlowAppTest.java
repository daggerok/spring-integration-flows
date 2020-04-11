package daggerok.basic;

import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.endpoint.MethodInvokingMessageSource;
import org.springframework.messaging.MessageChannel;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.integration.handler.LoggingHandler.Level.DEBUG;

@SpringBootApplication
class BasicFlowApp {
  public static void main(String[] args) {
    SpringApplication.run(BasicFlowApp.class, args);
  }
}

@Configuration
@EnableIntegration
class BasicFlowAppConfig {

  @Bean
  AtomicInteger atomicInteger() {
    return new AtomicInteger(0);
  }

  @Bean
  MessageSource<?> methodInvokingMessageSource() {
    var source = new MethodInvokingMessageSource();
    source.setObject(atomicInteger());
    source.setMethodName("incrementAndGet");
    return source;
  }

  @Bean
  MessageChannel inputChannel() {
    return MessageChannels.direct().get();
  }

  @Bean
  MessageChannel queue() {
    return MessageChannels.queue().get();
  }

  @Bean
  IntegrationFlow basicFlow() {
    var millis = Duration.ofMillis(234);
    return IntegrationFlows.from(methodInvokingMessageSource(),
                                 s -> s.poller(Pollers.fixedDelay(millis, millis))).log(DEBUG, "1-polled")
                           .channel(inputChannel()).log(DEBUG, "2-input-channel")
                           .filter((Integer i) -> i % 2 == 0).log(DEBUG, "3-filtered")
                           .transform(Object::toString).log(DEBUG, "4-transformed")
                           .channel(queue()).log(DEBUG, "5-queued")
                           .get();
  }
}

@Log4j2
@AllArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest(
    classes = BasicFlowApp.class,
    properties = "logging.level.org.springframework.integration=DEBUG")
class BasicFlowAppTest {

  IntegrationFlow basicFlow;

  @Test
  void test() {
    Try.run(() -> Thread.sleep(1234))
       .andFinally(() -> log.info("Basic integration flow {} complete.", basicFlow));
  }
}
