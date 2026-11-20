package br.fatec.hobby_hub.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

// Um plano na tela de troca, já com o efeito de escolhê-lo
public record OpcaoTrocaPlanoDTO(
        Long planoId,
        String nome,
        BigDecimal precoMensal,
        Integer creditosMensais,
        // ATUAL, UPGRADE, DOWNGRADE ou AGENDADO (downgrade já escolhido)
        String tipo,
        // Upgrade: diferença proporcional aos dias que faltam no ciclo. Demais: zero
        BigDecimal valorCobradoHoje,
        // Upgrade: créditos somados ao saldo na hora. Demais: zero
        Integer creditosAdicionais,
        // Quando o plano passa a valer
        LocalDate dataInicio
) {}
