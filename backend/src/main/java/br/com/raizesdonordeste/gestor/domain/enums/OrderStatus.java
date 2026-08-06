package br.com.raizesdonordeste.gestor.domain.enums;

/**
 * Ciclo de vida do pedido. Transicoes validas sao aplicadas em OrderService,
 * nao apenas neste enum, para manter a regra de negocio centralizada.
 */
public enum OrderStatus {
    CRIADO,
    AGUARDANDO_PAGAMENTO,
    PAGO,
    EM_PREPARO,
    PRONTO,
    ENTREGUE,
    CANCELADO
}
