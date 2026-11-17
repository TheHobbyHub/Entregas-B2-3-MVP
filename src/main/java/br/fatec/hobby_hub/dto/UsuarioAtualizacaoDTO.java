package br.fatec.hobby_hub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

// Dados editáveis na tela "Dados da Conta" (CPF não pode ser alterado)
public record UsuarioAtualizacaoDTO(
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotBlank(message = "O sobrenome é obrigatório")
        String sobrenome,

        @NotBlank(message = "O e-mail é obrigatório")
        @Pattern(
                regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                message = "Informe um e-mail válido (ex: exemplo@dominio.com)"
        )
        String email,

        @NotBlank(message = "O telefone é obrigatório")
        @Pattern(
                regexp = "^\\d{10,11}$",
                message = "O telefone deve conter 10 ou 11 dígitos numéricos com DDD"
        )
        String telefone,

        @Past(message = "A data de nascimento deve estar no passado")
        LocalDate dataNascimento
) {}
