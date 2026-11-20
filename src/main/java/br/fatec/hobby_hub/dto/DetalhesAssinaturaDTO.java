package br.fatec.hobby_hub.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

// Dados da tela Gerenciar Assinatura
public record DetalhesAssinaturaDTO(
        Boolean possuiAssinatura,
        Long planoId,
        String planoNome,
        BigDecimal precoMensal,
        Integer creditosMensais,
        Integer saldoCreditos,
        LocalDate dataInicioCiclo,
        LocalDate dataRenovacao,
        Boolean cancelamentoAgendado,
        // Downgrade que começa na data de renovação (null se não houver)
        String planoAgendadoNome,
        CartaoRespostaDTO cartao
) {}
