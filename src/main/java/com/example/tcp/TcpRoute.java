package com.example.tcp;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TcpRoute extends RouteBuilder {

    @Autowired
    private MessageRepository messageRepository;

    private final String tcpEndpoint = "netty:tcp://localhost:8080?sync=false&decoder=#stringDecoder&encoder=#stringEncoder";

    @Override
    public void configure() {
        from(tcpEndpoint)
                .process(exchange -> {
                    String message = exchange.getIn().getBody(String.class);
                    MessageEntity messageEntity = new MessageEntity();
                    messageEntity.setMessage(message);
                    messageRepository.save(messageEntity);
                });

        from("timer://dbPoller?fixedRate=true&period=5000")
                .process(exchange -> {
                    messageRepository.findAll().forEach(messageEntity -> {
                        exchange.getIn().setBody(messageEntity.getMessage());
                        exchange.getIn().setHeader("messageId", messageEntity.getId());
                    });
                })
                .to(tcpEndpoint)
                .process(exchange -> {
                    Long messageId = exchange.getIn().getHeader("messageId", Long.class);
                    messageRepository.deleteById(messageId);
                });
    }
}
