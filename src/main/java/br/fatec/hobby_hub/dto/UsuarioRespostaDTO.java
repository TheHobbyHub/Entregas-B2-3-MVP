package br.fatec.hobby_hub.dto;

import br.fatec.hobby_hub.infrastructure.entity.StatusUsuario;
import br.fatec.hobby_hub.infrastructure.entity.Usuario;

import java.time.LocalDate;

public record UsuarioRespostaDTO(
        Long id,
        String nome,
        String sobrenome,
        String email,
        String cpf,
        String telefone,
        LocalDate dataNascimento,
        StatusUsuario status,
        Integer saldoCreditos
) {
    public UsuarioRespostaDTO(Usuario usuario) {
        this(
                usuario.getId(),
                usuario.getNome(),
                usuario.getSobrenome(),
                usuario.getEmail(),
                usuario.getCpf(),
                usuario.getTelefone(),
                usuario.getDataNascimento(),
                usuario.getStatus(),
                usuario.getSaldoCreditos()
        );
    }
}
