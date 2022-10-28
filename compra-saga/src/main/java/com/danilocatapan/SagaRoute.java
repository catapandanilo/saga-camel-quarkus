package com.danilocatapan;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.SagaPropagation;
import org.apache.camel.saga.CamelSagaService;
import org.apache.camel.saga.InMemorySagaService;

@ApplicationScoped
public class SagaRoute extends RouteBuilder {

    @Inject
    PedidoService pedidoService;

    @Inject
    CreditoService creditoService;

    @Override
    public void configure() throws Exception {

        CamelSagaService sagaService = new InMemorySagaService(); // executar saga em memória.
        getContext().addService(sagaService);

        // nova transação, qual o caminho... alguem que faça a gestão da orquestração.
        from("direct:saga").saga().propagation(SagaPropagation.REQUIRES_NEW).log("Iniciando a transação")
                .to("direct:newPedido").log("Criando novo pedido com id ${header.id}")
                .to("direct:newPedidoValor").log("Resenvando o crédito")
                .to("direct:finaliza").log("Feito!");

        // PedidoService
        from("direct:newPedido").saga().propagation(SagaPropagation.MANDATORY)
                .compensation("direct:cancelPedido")
                .transform().header(Exchange.SAGA_LONG_RUNNING_ACTION)
                .bean(pedidoService, "newPedido").log("Pedido ${body} criado.");

        from("direct:cancelPedido")
                .transform().header(Exchange.SAGA_LONG_RUNNING_ACTION)
                .bean(pedidoService, "cancelPedido").log("Pedido ${body} cancelado.");

        // CreditoService
        from("direct:newPedidoValor").saga().propagation(SagaPropagation.MANDATORY)
                .compensation("direct:cancelPedidoValor")
                .transform().header(Exchange.SAGA_LONG_RUNNING_ACTION)
                .bean(creditoService, "newPedidoValor")
                .log("Crédito do pedido ${header.pedidoId} no valor ${header.valor} reservado para a saga ${body}.");

        from("direct:cancelPedidoValor")
                .transform().header(Exchange.SAGA_LONG_RUNNING_ACTION)
                .bean(creditoService, "cancelPedidoValor")
                .log("Crédito do pedido ${header.pedidoId} no valor ${header.valor} cancelado para a saga ${body}.");

        // Finaliza
        from("direct:finaliza").saga().propagation(SagaPropagation.MANDATORY)
                .choice()
                .end();

    }

}
