package br.fatec.hobby_hub.dto;

import br.fatec.hobby_hub.infrastructure.entity.Cartao;

public record CartaoRespostaDTO(
        String ultimosDigitos,
        String nomeTitular,
        String validade
) {
    public CartaoRespostaDTO(Cartao cartao) {
        this(cartao.getUltimosDigitos(), cartao.getNomeTitular(), cartao.getValidade());
    }
}
