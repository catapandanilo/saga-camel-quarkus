package com.danilocatapan;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CreditoService {

    private int creditoTotal;
    private Map<Long, Integer> pedido_valor = new HashMap<>();

    public CreditoService() {
        this.creditoTotal = 100;
    }

    // DO
    public void newPedidoValor(Long pedidoId, int valor) {
        if (valor > creditoTotal) {
            throw new IllegalStateException("Saldo Insuficiente");
        }

        creditoTotal = creditoTotal - valor;
        pedido_valor.put(pedidoId, valor);
    }

    // UNDO
    public void cancelPedidoValor(Long id) {
        creditoTotal = creditoTotal + pedido_valor.get(id);
        pedido_valor.remove(id);
    }

    public int getCreditoTotal() {
        return creditoTotal;
    }
}
