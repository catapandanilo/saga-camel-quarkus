package com.danilocatapan;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.apache.camel.Header;

@ApplicationScoped
public class CreditoService {

    private int creditoTotal;
    private Map<Long, Integer> pedido_valor = new HashMap<>();

    public CreditoService() {
        this.creditoTotal = 100;
    }

    // DO
    public void newPedidoValor(@Header("id") Long pedidoId, @Header("valor") int valor) {
        if (valor > creditoTotal) {
            throw new IllegalStateException("Saldo Insuficiente.");
        }

        creditoTotal = creditoTotal - valor;
        pedido_valor.put(pedidoId, valor);
    }

    // UNDO
    public void cancelPedidoValor(@Header("id")Long id) {
        System.out.println("PedidoValor falhou. Iniciando cancelamento do pedido.");
        // creditoTotal = creditoTotal + pedido_valor.get(id);
        // pedido_valor.remove(id);
    }

    public int getCreditoTotal() {
        return creditoTotal;
    }
}
