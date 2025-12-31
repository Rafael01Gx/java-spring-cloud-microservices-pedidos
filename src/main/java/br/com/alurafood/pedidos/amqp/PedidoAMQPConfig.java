package br.com.alurafood.pedidos.amqp;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PedidoAMQPConfig {

    @Bean
    public JacksonJsonMessageConverter rabbitMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, JacksonJsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    @Bean
    public Queue filaPedidos() {
        return QueueBuilder
                .durable("pagamentos.detalhes-pedido")
                .deadLetterExchange("pagamentos.dlx")
                .build();
    }

    @Bean
    public FanoutExchange fanoutPagamentosExchange() {
        return ExchangeBuilder
                .fanoutExchange("pagamentos.ex")
                .build();
    }


    @Bean
    public Binding pagamentoPedido() {
        return BindingBuilder
                .bind(filaPedidos())
                .to(fanoutPagamentosExchange());
    }

    @Bean
    public Queue filaDlq() {
        return QueueBuilder
                .durable("pagamentos.detalhes-pedido-dlq")
                .build();
    }

    @Bean
    public FanoutExchange deadLetterExchange() {
        return ExchangeBuilder
                .fanoutExchange("pagamentos.dlx")
                .build();
    }

    @Bean
    public Binding pagamentoPedidoDlx() {
        return BindingBuilder
                .bind(filaDlq())
                .to(deadLetterExchange());
    }
}
