package br.fatec.hobby_hub.dto;

import br.fatec.hobby_hub.infrastructure.entity.Fatura;
import br.fatec.hobby_hub.infrastructure.entity.TipoFatura;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FaturaRespostaDTO(
        Long id,
        LocalDate data,
        BigDecimal valor,
        TipoFatura tipo,
        String planoNome,
        String status
) {
    public FaturaRespostaDTO(Fatura fatura) {
        this(fatura.getId(), fatura.getData(), fatura.getValor(), fatura.getTipo(),
                fatura.getPlano().getNome(), fatura.getStatus());
    }
}
