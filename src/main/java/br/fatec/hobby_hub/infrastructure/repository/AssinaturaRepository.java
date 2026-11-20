package br.fatec.hobby_hub.infrastructure.repository;

import br.fatec.hobby_hub.infrastructure.entity.Assinatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AssinaturaRepository extends JpaRepository<Assinatura, Long> {
    List<Assinatura> findByUsuarioIdAndStatusIn(Long usuarioId, List<String> status);
    List<Assinatura> findByStatusAndDataVencimentoLessThanEqual(String status, LocalDate data);
}
