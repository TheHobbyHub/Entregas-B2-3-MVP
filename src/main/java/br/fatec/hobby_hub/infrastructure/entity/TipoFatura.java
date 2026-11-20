package br.fatec.hobby_hub.infrastructure.entity;

public enum TipoFatura {
    // Cobrança do mês cheio (primeira assinatura e renovações)
    MENSALIDADE,
    // Diferença proporcional cobrada ao trocar para um plano mais caro no meio do ciclo
    UPGRADE
}
