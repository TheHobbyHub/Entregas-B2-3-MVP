package br.fatec.hobby_hub.dto;

import jakarta.validation.Valid;

public record AssinaturaRequestDTO(
        Long usuarioId,
        Long planoId,
        String metodoPagamento,
        // Cartão de cobrança, guardado para exibir em Gerenciar Assinatura e cobrar upgrades
        @Valid CartaoRequestDTO cartao
) {}
