package br.fatec.hobby_hub.dto;

import jakarta.validation.constraints.NotNull;

public record TrocaPlanoRequestDTO(
        @NotNull(message = "Informe o plano")
        Long planoId
) {}
