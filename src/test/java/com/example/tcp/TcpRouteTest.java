package com.example.tcp;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@CamelSpringBootTest
@SpringBootTest
class TcpRouteTest {

//    @Autowired
//    private CamelContext camelContext;
//
//    @Autowired
//    private ModelCamelContext modelCamelContext;
//
//    @Autowired
//    private MessageRepository messageRepository;
//
//    @Test
//    void testTcpRoute() throws Exception {
//        MockEndpoint mockTcp = camelContext.getEndpoint("mock:tcp", MockEndpoint.class);
//
//        Optional<RouteDefinition> tcpRouteDefinition = modelCamelContext.getRouteDefinitions().stream()
//                .filter(routeDefinition -> routeDefinition.getId().equals("tcpRoute"))
//                .findFirst();
//
//        tcpRouteDefinition.ifPresent(routeDefinition -> routeDefinition.adviceWith(modelCamelContext, new AdviceWithRouteBuilder() {
//            @Override
//            public void configure() {
//                replaceFromWith("direct:tcp");
//                interceptSendToEndpoint("netty:tcp://localhost:8080?sync=false&decoder=#stringDecoder&encoder=#stringEncoder")
//                        .skipSendToOriginalEndpoint()
//                        .to("mock:tcp");
//            }
//        }));
//
//        ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
//
//        String testMessage = "Test Message";
//        producerTemplate.sendBody("direct:tcp", testMessage);
//
//        MessageEntity savedMessage = messageRepository.findAll().get(0);
//        assertThat(savedMessage.getMessage()).isEqualTo(testMessage);
//
//        mockTcp.expectedMessageCount(1);
//        mockTcp.assertIsSatisfied();
//
//        assertThat(messageRepository.findAll()).isEmpty();
//    }

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private MessageRepository messageRepository;

    @Test
    void testTcpRoute() throws Exception {
        Predicate predicate = exchange -> {
            String body = exchange.getIn().getBody(String.class);
            return body != null && body.equals("Test Message");
        };
        NotifyBuilder notifyBuilder = new NotifyBuilder(camelContext)
                .fromRoute("tcpRoute")
                .whenAnyDoneMatches(predicate)
                .create();

        ProducerTemplate producerTemplate = camelContext.createProducerTemplate();

        String testMessage = "Test Message";
        producerTemplate.sendBody("netty:tcp://localhost:8080?sync=false&decoder=#stringDecoder&encoder=#stringEncoder", testMessage);

        assertThat(notifyBuilder.matches(5, TimeUnit.SECONDS)).isTrue();

        MessageEntity savedMessage = messageRepository.findAll().get(0);
        assertThat(savedMessage.getMessage()).isEqualTo(testMessage);

        assertThat(messageRepository.findAll()).isEmpty();
    }
}