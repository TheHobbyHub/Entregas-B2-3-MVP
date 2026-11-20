package br.fatec.hobby_hub.infrastructure.repository;

import br.fatec.hobby_hub.infrastructure.entity.Cartao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartaoRepository extends JpaRepository<Cartao, Long> {
    Optional<Cartao> findByUsuarioId(Long usuarioId);
}
