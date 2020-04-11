package daggerok.ws;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.ws.SimpleWebServiceOutboundGateway;
import org.springframework.integration.ws.WebServiceHeaders;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement//(namespace = "https://www.w3schools.com/xml/")
@XmlAccessorType(XmlAccessType.FIELD)
class FahrenheitToCelsiusResponse {

  @XmlElement(
      name = "FahrenheitToCelsiusResult",
      namespace = "https://www.w3schools.com/xml/" // important!
  )
  private float fahrenheitToCelsiusResult;
}

@Log4j2
@EnableIntegration
@SpringBootApplication
class TemperatureConverterApp {

  @MessagingGateway
  interface TempConverter {

    @Gateway(requestChannel = "convert.input")
    double toCelcius(float fahrenheit);

    // @Gateway(requestChannel = "convert.input")
    // float toFahrenheit(float celcius);
  }

  @Bean
  public IntegrationFlow convert() {
    // // // Celsius -> Fahrenheit
    // return f -> f.transform(payload -> "<CelsiusToFahrenheit xmlns=\"https://www.w3schools.com/xml/\">"
    //                                  + "  <Celsius>" + payload + "</Celsius>"
    //                                  + "</CelsiusToFahrenheit>")
    // Fahrenheit -> Celsius
    return f -> f.transform(payload -> "<FahrenheitToCelsius xmlns=\"https://www.w3schools.com/xml/\">"
                                     + "  <Fahrenheit>" + payload + "</Fahrenheit>"
                                     + "</FahrenheitToCelsius>")
                 .enrichHeaders(h -> h.header(WebServiceHeaders.SOAP_ACTION, "https://www.w3schools.com/xml/FahrenheitToCelsius"))
                 .handle(new SimpleWebServiceOutboundGateway("https://www.w3schools.com/xml/tempconvert.asmx"))
                 .transform((String source) -> {
                   var response = JAXB.unmarshal(new StringReader(source), FahrenheitToCelsiusResponse.class);
                   var result = response.getFahrenheitToCelsiusResult();
                   log.warn(result);
                   return result;
                 });
                 // .transform(Transformers.xpath("/*[local-name()=\"FahrenheitToCelsiusResponse\"]"
                 //                                   + "/*[local-name()=\"FahrenheitToCelsiusResult\"]"));
  }

  public static void main(String[] args) {
    SpringApplication.run(TemperatureConverterApp.class, args);
    // // String s = "<FahrenheitToCelsiusResponse><FahrenheitToCelsiusResult>32.2222222222222</FahrenheitToCelsiusResult></FahrenheitToCelsiusResponse>";
    // String s = "<FahrenheitToCelsiusResponse xmlns=\"https://www.w3schools.com/xml/\"><FahrenheitToCelsiusResult>33.8888888888889</FahrenheitToCelsiusResult></FahrenheitToCelsiusResponse>";
    // FahrenheitToCelsiusResponse res = JAXB.unmarshal(new StringReader(s), FahrenheitToCelsiusResponse.class);
    // System.out.println(res.getFahrenheitToCelsiusResult());
  }
}

@Log4j2
@AllArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest(classes = TemperatureConverterApp.class)
class TemperatureConverterTest {

  TemperatureConverterApp.TempConverter converter;

  @Test
  void test() {
    var celcius = converter.toCelcius(93);
    log.info(celcius);
    assertThat(Double.valueOf(celcius).intValue()).isEqualTo(33);
  }
}
