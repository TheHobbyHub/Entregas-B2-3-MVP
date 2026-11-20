package br.fatec.hobby_hub.infrastructure.repository;

import br.fatec.hobby_hub.infrastructure.entity.Fatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaturaRepository extends JpaRepository<Fatura, Long> {
    List<Fatura> findByAssinaturaUsuarioIdOrderByDataDescIdDesc(Long usuarioId);
}
