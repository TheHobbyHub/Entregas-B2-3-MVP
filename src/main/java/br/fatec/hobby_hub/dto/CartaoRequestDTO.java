package br.fatec.hobby_hub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// O CVV é conferido só no app e nunca é enviado nem guardado
public record CartaoRequestDTO(
        @NotBlank(message = "Informe o número do cartão")
        @Pattern(regexp = "^[\\d ]{13,23}$", message = "Número do cartão inválido")
        String numero,

        @NotBlank(message = "Informe o nome impresso no cartão")
        String nomeTitular,

        @NotBlank(message = "Informe a validade")
        @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$", message = "Use a validade no formato MM/AA")
        String validade
) {}
